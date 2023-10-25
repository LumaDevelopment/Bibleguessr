# Authorization Microservice

- Store salted + hashed passwords in the database
- Consider what information should be stored about a user
- Require the user to make a 12-character password with symbols. Hash with SHA256.
- No password expiration.
- UUID for each user allows the user to change their username
- No SQL injection allowed. No special characters.
- Database backups to loss of data from hacks.
- Maintain a username list to make searching for new users quicker.
- The token contains the UUID, and the web app can access the user's data from the token.
- Store the user's token in a cookie.
- Authentication server distributes tokens (maybe OAuth) that expire, contain information about the user, etc.
- Authentication tokens are set in browser cookies
- Password is hashed, sent to the database, database compares, and then returns the token if there’s a match.
- Maybe associate the token with IP, so if the token is used from a different IP, then invalidate it.
- Built-in database encryption, probably MongoDB, is best.
- MongoDB will be used for both account data and account authentication.
- https://auth0.com/pricing 
