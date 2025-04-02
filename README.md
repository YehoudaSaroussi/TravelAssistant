# Travel Assistant Application

A Spring Boot application that uses AI to help users plan and organize their travels through natural language conversations.

## Description

Travel Assistant is an interactive AI-powered application that uses the Google Gemini API to provide personalized travel advice and recommendations. It helps users discover destinations, learn about local attractions, create packing lists, and access essential travel information through a conversational interface.

## Technology Stack

- Java 22
- Spring Boot 3.2.5
- Thymeleaf template engine
- Maven for dependency management
- Google Gemini 1.5 Flash LLM API
- Open-Meteo Weather API
- RestCountries API

## Features

- **AI-Powered Travel Conversations**: Interact naturally with an AI assistant trained to provide travel advice
- **Destination Recommendations**: Get personalized destination suggestions based on your interests, budget, and preferences
- **Local Attractions**: Discover popular tourist spots, cultural landmarks, and hidden gems at your destination
- **Packing Suggestions**: Receive customized packing lists based on your destination, duration, and planned activities
- **Weather Information**: Get current weather data for your destinations to help with planning
- **Country Information**: Access details about currencies, languages, and other country-specific information
- **Conversation History**: Save and revisit past conversations organized by topic
- **Multi-User Support**: Switch between different user profiles to maintain separate conversation histories
- **Contextual Suggestions**: Receive relevant follow-up question suggestions based on conversation context

## Setup and Installation

### Prerequisites

- JDK 17 or later (JDK 22 recommended)
- Maven 3.6 or later

### Running Locally

1. Clone the repository

   ```
   git clone https://github.com/YOUR_USERNAME/travel-assistant.git
   cd travel-assistant
   ```

2. Build the project

   ```
   mvn clean install
   ```

3. Run the application

   ```
   mvn spring-boot:run
   ```

4. Access the application at http://localhost:8080

## Usage

### Getting Started

1. Open the application in your browser at http://localhost:8080
2. Click the "Start Chat" button on the homepage to open the chat interface
3. Type your travel-related questions in the input field and press Enter or click the send button
4. View the AI assistant's responses and follow-up suggestions

### Example Queries

- "Where should I travel in May if I love beaches and culture?"
- "What are the top attractions in Paris?"
- "What should I pack for a 2-week trip to Japan in winter?"
- "Tell me about the weather in Thailand during November"
- "What's the currency used in Argentina?"
- "What's the best time to visit Australia?"
- "How should I get around in London?"

### Using Conversation Features

- **New Chat**: Click the "+ New Chat" button in the sidebar to start a fresh conversation
- **Chat History**: View and click on past conversations in the sidebar to revisit them
- **Suggestions**: Click on the suggestion chips below the chat for quick follow-up questions
- **Change User**: Click the "Change User" button to switch between different user profiles

## API Documentation

The application exposes the following REST API endpoints:

### Conversation API

**POST /api/conversation**

Process a user message and return an AI-generated response.

Request body:

```json
{
  "userId": "string",
  "message": "string",
  "context": "string (optional)"
}
```

Response:

```json
{
  "responseMessage": "string",
  "suggestions": ["string", "string", ...],
  "isError": boolean
}
```

## External API Integration

The application integrates with the following external APIs:

- **Google Gemini API**: Provides AI-powered responses to user queries
- **Open-Meteo Weather API**: Retrieves current weather data for locations
- **RestCountries API**: Provides country information like capitals, currencies, and languages

## Configuration

Key configuration properties in `application.properties`:

```properties
# Gemini API Configuration
gemini.api.url=https://generativelanguage.googleapis.com
gemini.api.key=YOUR_API_KEY
gemini.api.model-name=gemini-1.5-flash

# Weather API Configuration (Open-Meteo)
weather.geocoding.api.url=https://geocoding-api.open-meteo.com/v1/search
weather.forecast.api.url=https://api.open-meteo.com/v1/forecast

# Country Information API
countryinfo.api.url=https://restcountries.com/v3.1/name
```

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add some amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Examples

See [conversation examples](EXAMPLES.md) for screenshots showing sample interactions with the Travel Assistant.
