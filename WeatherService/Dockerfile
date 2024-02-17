# Use the official Golang image to create a build artifact.
# This is based on Debian and sets the GOPATH to /go.
FROM golang:1.22.0 as builder

# Copy local code to the container image.
WORKDIR /app
COPY . .

# Build the command inside the container.
# (Assuming your main package is in the root directory)
RUN CGO_ENABLED=0 GOOS=linux go build -v -o server .

# Use a Docker multi-stage build to create a lean production image.
FROM alpine:latest  
RUN apk --no-cache add ca-certificates

WORKDIR /root/
# Copy the binary to the production image from the builder stage.
COPY --from=builder /app/server .

# Run the web service on container startup.
CMD ["./server"]
