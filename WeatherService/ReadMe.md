# Weather Service

A http server that uses the Open Weather API that exposes an endpoint that takes in lat/long
coordinates.

## How to run the service

1. Clone the repository
2. Set the following environment variables
    - OPEN_WEATHER_API_KEY: Your Open Weather API key
    - OPEN_WEATHER_API_URL: The Open Weather API URL

    ```bash
    export OPEN_WEATHER_API_KEY=<your_open_weather_api_key>
    export OPEN_WEATHER_API_URL=https://api.openweathermap.org/data/2.5/weather
    ```

3. Run the following command to start the service

```bash
go run main.go
```

The service can also be run using docker. To do this, run the following command

```bash
make run
```

## How to use the service

The service exposes an endpoint that takes in lat/long coordinates and returns the weather at that
location.

The endpoint is `GET /weather` and takes in two query parameters `lat` and `long` which are the
latitude and longitude coordinates of the location you want to get the weather for.

Example:

```bash
curl http://localhost:8080/weather?lat=35&long=139
```
