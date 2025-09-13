import requests
import time
from datetime import datetime
from concurrent.futures import ThreadPoolExecutor

# Base URL of your application
BASE_URL = "http://localhost:8080"

# List of all API endpoints to test
ENDPOINTS = [
    # Common endpoints
    {"method": "GET", "path": "/social-app/test"},
    {"method": "GET", "path": "/social-app/beans"},
    
    # WhatsApp endpoints
    {"method": "GET", "path": "/social-app/wa/traverse"},
    {"method": "GET", "path": "/social-app/wa/unarchive"},
    {"method": "GET", "path": "/social-app/wa/archive"},
    {"method": "GET", "path": "/social-app/wa/broadcast"},
    {"method": "GET", "path": "/social-app/wa/broadcast/known_clients"},
    {"method": "GET", "path": "/social-app/wa/broadcast/unknown_clients"},
    {"method": "GET", "path": "/social-app/wa/clear/already_broadcasted"},
    {"method": "GET", "path": "/social-app/wa/clear/already_traversed"},
    
    # Telegram endpoints
    {"method": "GET", "path": "/social-app/tg/traverse"},
    {"method": "GET", "path": "/social-app/tg/broadcast"},
    
    # Facebook endpoints
    {"method": "GET", "path": "/social-app/fb/broadcast/skillset"},
    {"method": "GET", "path": "/social-app/fb/broadcast/sensitive"},
    {"method": "GET", "path": "/social-app/fb/broadcast/offshore"},
    
    # Log endpoints
    {"method": "GET", "path": "/api/logs"}
]

def test_endpoint(endpoint):
    url = f"{BASE_URL}{endpoint['path']}"
    method = endpoint['method']
    
    print(f"\nTesting {method} {url}")
    print("-" * 50)
    
    start_time = time.time()
    success_count = 0
    error_count = 0
    response_times = []
    
    try:
        # Test for 10 seconds
        while time.time() - start_time < 10:
            try:
                request_start = time.time()
                
                if method == "GET":
                    response = requests.get(url, timeout=5, stream=True if endpoint['path'] == '/api/logs' else False)
                # Add other methods if needed
                
                response_time = (time.time() - request_start) * 1000  # in milliseconds
                response_times.append(response_time)
                
                if 200 <= response.status_code < 300:
                    success_count += 1
                    print(f"✓ Success: {response.status_code} - {response_time:.2f}ms")
                else:
                    error_count += 1
                    print(f"✗ Error: {response.status_code} - {response.text}")
                
                # For streaming endpoints, read some data to verify
                if endpoint['path'] == '/api/logs':
                    for _ in range(3):  # Read a few lines to verify streaming
                        next(response.iter_lines())
                
            except requests.exceptions.RequestException as e:
                error_count += 1
                print(f"✗ Request failed: {str(e)}")
            
            # Small delay between requests
            time.sleep(0.5)
                
    except KeyboardInterrupt:
        print("\nTest interrupted by user")
    
    # Calculate statistics
    total_requests = success_count + error_count
    avg_response_time = sum(response_times) / len(response_times) if response_times else 0
    success_rate = (success_count / total_requests * 100) if total_requests > 0 else 0
    
    return {
        "endpoint": f"{method} {endpoint['path']}",
        "total_requests": total_requests,
        "success_count": success_count,
        "error_count": error_count,
        "success_rate": success_rate,
        "avg_response_time": avg_response_time,
        "min_response_time": min(response_times) if response_times else 0,
        "max_response_time": max(response_times) if response_times else 0
    }

def main():
    print("Starting API Load Test")
    print("=" * 50)
    
    results = []
    
    # Test each endpoint one by one
    for endpoint in ENDPOINTS:
        result = test_endpoint(endpoint)
        results.append(result)
    
    # Print summary
    print("\n" + "=" * 50)
    print("TEST SUMMARY")
    print("=" * 50)
    
    for result in results:
        print(f"\n{result['endpoint']}")
        print(f"  Total Requests: {result['total_requests']}")
        print(f"  Success: {result['success_count']}")
        print(f"  Errors: {result['error_count']}")
        print(f"  Success Rate: {result['success_rate']:.2f}%")
        print(f"  Avg Response Time: {result['avg_response_time']:.2f}ms")
        print(f"  Min/Max Response Time: {result['min_response_time']:.2f}ms / {result['max_response_time']:.2f}ms")
    
    # Calculate overall statistics
    total_requests = sum(r['total_requests'] for r in results)
    total_errors = sum(r['error_count'] for r in results)
    overall_success_rate = ((total_requests - total_errors) / total_requests * 100) if total_requests > 0 else 0
    
    print("\n" + "=" * 50)
    print("OVERALL STATISTICS")
    print("=" * 50)
    print(f"Total Endpoints Tested: {len(results)}")
    print(f"Total Requests: {total_requests}")
    print(f"Total Errors: {total_errors}")
    print(f"Overall Success Rate: {overall_success_rate:.2f}%")
    print("\nTest completed at", datetime.now().strftime("%Y-%m-%d %H:%M:%S"))

if __name__ == "__main__":
    main()
