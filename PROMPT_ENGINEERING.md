# Prompt Engineering Decisions

This document outlines the key prompt engineering techniques and design decisions implemented in the Travel Assistant application.

## Core Strategies

### Template-Based Prompts

- **Specialized Templates**: The application uses distinct prompt templates for different query types:
  - Destination recommendations
  - Local attractions discovery
  - Packing suggestions
- **Dynamic Placeholders**: Templates contain placeholders (`{destination}`, `{duration}`, `{activities}`) that are replaced with extracted values from user input
- **Contextual Enrichment**: Templates incorporate conversation history and external data from weather and country information APIs

### Entity Extraction

The system automatically identifies key entities in user messages:

- **Destinations**:

  - Identifies location names following prepositions ("in Paris", "to Japan")
  - Uses indicators like "in", "to", "at", "for" to locate possible destinations

- **Durations**:

  - Extracts time periods ("3 days", "2 weeks")
  - Looks for numeric values followed by time units (days, weeks, months, nights)

- **Activities**:
  - Recognizes activity types mentioned (hiking, swimming, skiing, etc.)
  - Augments with "general tourism" as a default

### Context Management

- Maintains conversation context with `ContextManager`
- Implements a maximum context length (200 characters) to prevent token overflow
- Truncates when necessary while preserving the most relevant information
- Stores user-specific conversation history separately for personalized experiences
- Uses a `MAX_MESSAGES` limit (10) in the conversation history to keep context relevant and current

### Prompt Optimization

- **Token Economy**: Carefully manages prompt length to reduce token usage and API costs
- **Context Truncation**: Implements strategic truncation at word boundaries to maintain coherence
- **Message Summarization**: For longer user messages, creates a brief summary for context preservation
  - Uses `SUMMARY_LENGTH` constant (200 characters) to limit the size of message summaries
  - Focuses on extracting the primary intent and key entities for minimal but effective context

### API Integration Strategy

- **Hybrid Knowledge Approach**: Combines LLM's general knowledge with real-time API data
- **Selective API Enrichment**: Analyzes user queries to determine when external data would improve responses
  - Weather-related queries trigger API calls to Open-Meteo
  - Country information queries activate calls to RestCountries API
- **Contextual Data Injection**: Enriches prompts with external data only when relevant
  - Example: "What's the weather in Paris?" triggers weather API data inclusion in prompt
  - Results in more accurate, timely responses without burdening every prompt

### Advanced Prompting Techniques

- **Chain-of-Thought Prompting**: For complex questions, guides the model through a step-by-step reasoning process:
  1. Understanding the information need
  2. Considering relevant factors (season, budget, interests)
  3. Providing specific, actionable recommendations
- **Prefixed Instructions**: Each prompt begins with "You are a helpful travel assistant" to maintain consistent tone and expertise
- **Few-Shot Examples**: For specialized responses (like itinerary creation), provides examples in the prompt to guide formatting

### Prompt Construction

The `createPrompt` method assembles prompts by:

1. Retrieving relevant conversation context
2. Loading the appropriate template based on query type
3. Extracting entities from user input
4. Replacing placeholders with extracted values
5. Adding context information

## Implementation Details

Each prompt type has specialized handling:

- **Destination Recommendations**: Focus on user preferences and timing
- **Local Attractions**: Emphasize landmarks, activities, and local experiences
- **Packing Suggestions**: Consider destination, weather, duration, and activities
- **General Queries**: Maintains conversational context without specialized formatting

## Response Formatting

- **Structured Output**: Templates encourage consistent response formatting for better user experience
- **Suggestion Generation**: Dynamically generates related follow-up questions based on conversation topic
- **Length Control**: Uses temperature and max_output_tokens parameters to manage response verbosity

## Benefits of This Approach

- **Consistent Quality**: Templates ensure well-structured prompts that lead to more reliable responses
- **Contextual Awareness**: Responses maintain coherence across conversation turns
- **Precision**: Entity extraction focuses the model on relevant information
- **Adaptability**: Different prompt types for different user needs
- **Efficiency**: Optimized prompts reduce token consumption and API costs
- **Reliability**: External API data ensures factual accuracy for time-sensitive information

## Future Improvements

Potential enhancements to the prompt engineering system:

- More sophisticated entity extraction using NLP libraries
- Fine-tuning prompts based on user feedback
- Dynamic template selection based on more nuanced intent recognition
- Expanding chain-of-thought prompting to more query types
- Implementing structured output parsing for complex response types like itineraries
- Adding semantic caching for similar questions to reduce API calls
- Integrating embeddings for better contextual understanding of user queries
