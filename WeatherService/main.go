package main

import (
	"encoding/json"
	"fmt"
	"io"
	"log"
	"net/http"
	"os"
	"strconv"
	"time"
)

// Temperature classification constants
const (
	HotTemperature      = 30 // degrees Celsius
	ColdTemperature     = 15 // degrees Celsius
	ModerateTemperature = 20 // degrees Celsius
	OpenAPIWeatherUrl   = "http://api.openweathermap.org/data/2.5/weather"
)

type WeatherService interface {
	GetWeather(lat, lon string) (*WeatherResponse, error)
}

type Service struct {
	APIKey            string
	OpenAPIWeatherUrl string
}

// WeatherResponse is used to decode the JSON response from OpenWeather
type WeatherResponse struct {
	Weather []struct {
		Main string `json:"main"`
	} `json:"weather"`
	Main struct {
		Temp float64 `json:"temp"`
	} `json:"main"`
}

func (s *Service) GetWeather(lat, lon string) (*WeatherResponse, error) {
	// Construct the API call URL
	url := fmt.Sprintf("%s?lat=%s&lon=%s&appid=%s&units=metric", OpenAPIWeatherUrl, lat, lon, s.APIKey)

	fmt.Println(url)

	// Construct the API call URL
	req, err := http.NewRequest("GET", url, nil)
	if err != nil {
		return nil, err
	}

	// Make the request
	resp, err := http.DefaultClient.Do(req)
	if err != nil {
		return nil, err
	}
	defer resp.Body.Close()

	// Read the response body
	body, err := io.ReadAll(resp.Body)
	if err != nil {
		return nil, err
	}

	// Decode the JSON response
	var weatherResponse WeatherResponse
	err = json.Unmarshal(body, &weatherResponse)
	if err != nil {
		return nil, err
	}

	return &weatherResponse, nil
}

func classifyTemperature(temp float64) string {
	switch {
	case temp >= HotTemperature:
		return "Hot"
	case temp <= ColdTemperature:
		return "Cold"
	case temp >= ModerateTemperature:
		return "Moderate"
	default:
		return "Cold"
	}
}

// isValidLatitude checks if a latitude value is valid.
func isValidLatitude(lat string) bool {
	latitude, err := strconv.ParseFloat(lat, 64)
	if err != nil {
		return false
	}
	return latitude >= -90 && latitude <= 90
}

// isValidLongitude checks if a longitude value is valid.
func isValidLongitude(lon string) bool {
	longitude, err := strconv.ParseFloat(lon, 64)
	if err != nil {
		return false
	}
	return longitude >= -180 && longitude <= 180
}

// weatherHandler returns an http.HandlerFunc that uses the provided WeatherService
func weatherHandler(service WeatherService) http.HandlerFunc {
	return func(w http.ResponseWriter, r *http.Request) {
		// Parse latitude and longitude from query parameters
		lat := r.URL.Query().Get("lat")
		lon := r.URL.Query().Get("lon")

		// Validate latitude and longitude
		if !isValidLatitude(lat) || !isValidLongitude(lon) {
			http.Error(w, "Invalid latitude or longitude", http.StatusBadRequest)
			return
		}

		// Call OpenWeather API
		weather, err := service.GetWeather(lat, lon)
		if err != nil {
			http.Error(w, err.Error(), http.StatusInternalServerError)
			return
		}

		// Write the result as a plain text response
		w.Header().Set("Content-Type", "text/plain")

		if weather == nil || len(weather.Weather) == 0 {
			w.WriteHeader(http.StatusInternalServerError)
			fmt.Fprintf(w, "No weather data available")
			return
		}

		w.WriteHeader(http.StatusOK)
		response := fmt.Sprintf("Weather: %s, Temperature: %s", weather.Weather[0].Main, classifyTemperature(weather.Main.Temp))
		fmt.Fprint(w, response)
	}
}

func main() {
	var service Service

	if value, exists := os.LookupEnv("OPENWEATHER_API_KEY"); exists {
		service.APIKey = value
	}

	if service.APIKey == "" {
		log.Fatal("OPENWEATHER_API_KEY environment variable is not set")
	}

	if value, exists := os.LookupEnv("OPENWEATHER_API_URL"); exists {
		service.OpenAPIWeatherUrl = value
	} else {
		service.OpenAPIWeatherUrl = OpenAPIWeatherUrl
	}

	// Setup the HTTP server and routing
	server := &http.Server{
		Addr:         ":8080",
		ReadTimeout:  5 * time.Second,  // Set the read timeout to 5 seconds
		WriteTimeout: 10 * time.Second, // Set the write timeout to 10 seconds
	}

	// Register the weatherHandler function to handle requests
	http.HandleFunc("/weather", weatherHandler(&service))

	// Start the server
	fmt.Println("Starting server on :8080")
	log.Fatal(server.ListenAndServe())
}
