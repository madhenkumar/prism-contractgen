import os
import shutil
import subprocess
from fastapi import FastAPI, HTTPException
from fastapi.responses import FileResponse

app = FastAPI(title="Contract Generation Pipeline")

# We run in the contract directory
CONTRACT_DIR = os.path.dirname(os.path.abspath(__file__))

@app.post("/consumer")
async def generate_contracts():
    # Force UTF-8 encoding in child Python processes to prevent UnicodeEncodeError on Windows
    # when printing emojis like ✅.
    env = os.environ.copy()
    env["PYTHONIOENCODING"] = "utf-8"

    try:
        # 1. Clear the tests folder if it exists
        tests_dir = os.path.join(CONTRACT_DIR, "tests")
        if os.path.exists(tests_dir):
            shutil.rmtree(tests_dir)

        # 2. Execute openapi_generator.py
        print("Running openapi_generator.py...")
        result_openapi = subprocess.run(
            ["python", "openapi_generator.py"], 
            cwd=CONTRACT_DIR, 
            capture_output=True, 
            text=True,
            encoding="utf-8",
            env=env
        )
        if result_openapi.returncode != 0:
            raise HTTPException(status_code=500, detail=f"openapi_generator.py failed: {result_openapi.stderr}")

        # 3. Execute llm_contract_generator.py
        print("Running llm_contract_generator.py...")
        result_llm = subprocess.run(
            ["python", "llm_contract_generator.py"], 
            cwd=CONTRACT_DIR, 
            capture_output=True, 
            text=True,
            encoding="utf-8",
            env=env
        )
        if result_llm.returncode != 0:
            raise HTTPException(status_code=500, detail=f"llm_contract_generator.py failed: {result_llm.stderr}")

        # 4. Execute pact_generator.py
        print("Running pact_generator.py...")
        result_pact = subprocess.run(
            ["python", "pact_generator.py"], 
            cwd=CONTRACT_DIR, 
            capture_output=True, 
            text=True,
            encoding="utf-8",
            env=env
        )
        if result_pact.returncode != 0:
            raise HTTPException(status_code=500, detail=f"pact_generator.py failed: {result_pact.stderr}")

        # 5. Zip the tests directory
        print("Creating zip archive of tests folder...")
        if not os.path.exists(tests_dir) or not os.listdir(tests_dir):
            raise HTTPException(status_code=500, detail="Tests directory is empty or not created.")
            
        zip_base_path = os.path.join(CONTRACT_DIR, "tests_archive") # Will create tests_archive.zip
        shutil.make_archive(zip_base_path, 'zip', tests_dir)
        
        final_zip = zip_base_path + ".zip"

        # Return the ZIP file back to the user
        return FileResponse(
            path=final_zip, 
            media_type="application/zip", 
            filename="contracts_tests.zip"
        )

    except HTTPException:
        raise
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))
