# Guess Counter Service

## Service Features

- Keeps track of the number of guesses that have been made on Bibleguessr.
- Keeps two counts of guesses: one in memory, and one in a file.
- The in memory count is a buffer that is added with the file count and written to the file at an interval.
- The file count is stored in a file and persistent between runs.

## Class Structure & Descriptions

- `gg.bibleguessr.guess_counter`
    - `gg.bibleguessr.guess_counter.requests`
        - `GetCountRequest` - Represents a request for the current guess count.
        - `IncrementCountRequest` - Represents a request to increment the guess count.
    - `CounterFileMgr` - Ensures thread-safe access to the persistent file that stores the guess count.
    - `GuessCounterService` - The main `Microservice` class that fields requests, regularly updates the counter file,
      etc.
    - `GuessCounterServiceConfig` - The configuration class for the Guess Counter Service. Allows you to configure the
      name of the counter file and the interval at which to update it.