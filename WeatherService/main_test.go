package main

import (
	"fmt"
	"net/http"
	"net/http/httptest"
	"testing"

	"github.com/stretchr/testify/assert"
	"github.com/stretchr/testify/mock"
)

// MockWeatherService is a mock type for the WeatherService
type MockWeatherService struct {
	mock.Mock
}

// GetWeather is the mock method that simulates fetching weather data.
func (m *MockWeatherService) GetWeather(lat, lon string) (*WeatherResponse, error) {
	args := m.Called(lat, lon)
	return args.Get(0).(*WeatherResponse), args.Error(1)
}

type weatherTest struct {
	mockError    error
	mockResponse *WeatherResponse
	lat          string
	lon          string
	expectedBody string
	expectedCode int
}

func TestWeatherHandler(t *testing.T) {
	// Create a slice of test cases
	tests := []weatherTest{
		{
			lat:          "40.7128",
			lon:          "-74.0060",
			expectedBody: "Weather: Clouds, Temperature: Cold",
			expectedCode: http.StatusOK,
			mockResponse: &WeatherResponse{
				Main: struct {
					Temp float64 `json:"temp"`
				}{Temp: 15},
				Weather: []struct {
					Main string `json:"main"`
				}{{Main: "Clouds"}},
			},
			mockError: nil,
		},
		{
			lat:          "invalid",
			lon:          "invalid",
			expectedBody: "Invalid latitude or longitude",
			expectedCode: http.StatusBadRequest,
			mockResponse: nil,
			mockError:    fmt.Errorf("invalid coordinates"),
		},
		{
			lat:          "40.7128",
			lon:          "-74.0060",
			expectedBody: "No weather data available",
			expectedCode: http.StatusInternalServerError,
			mockResponse: nil,
			mockError:    nil,
		},
		{
			lat:          "40.7128",
			lon:          "-74.0060",
			expectedBody: "invalid coordinates",
			expectedCode: http.StatusInternalServerError,
			mockResponse: nil,
			mockError:    fmt.Errorf("invalid coordinates"),
		},
	}

	for _, tc := range tests {
		// Setup the mock service
		mockService := new(MockWeatherService)
		mockService.On("GetWeather", tc.lat, tc.lon).Return(tc.mockResponse, tc.mockError)

		// Setup the handler
		handler := weatherHandler(mockService)

		// Create an HTTP request
		req, err := http.NewRequest("GET", fmt.Sprintf("/weather?lat=%s&lon=%s", tc.lat, tc.lon), nil)
		assert.NoError(t, err)

		// Record the response
		w := httptest.NewRecorder()
		handler.ServeHTTP(w, req)

		// Assert the response code
		assert.Equal(t, tc.expectedCode, w.Code)

		// Assert the response body
		if tc.expectedBody != "" {
			assert.Contains(t, w.Body.String(), tc.expectedBody)
		}

		// Assert that the expectations were met
		if tc.mockError == nil {
			mockService.AssertExpectations(t)
		}
	}
}
