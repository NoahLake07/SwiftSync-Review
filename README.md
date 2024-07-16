# SwiftSync Media Reviewer

SwiftSync Media Reviewer is a Java-based application designed to streamline the process of remote media review for film crews. This application allows film directors, DITs, producers, and other crew members to review media remotely and make decisions on the go.

## Features

- **Session Creation**: The host can create a session that other crew members can join. This session serves as a collaborative platform for media review.

- **Live Media Review**: The host leads the media review, controlling what media everyone is looking at live. This ensures that all crew members are on the same page.

- **Messaging**: The application includes a messaging feature that allows crew members to communicate and make decisions about the media being reviewed.

- **Local Proxy Playback**: To ensure smooth playback without streaming errors, each crew member needs a copy of proxies on their end. The server instructs the clients of the session on what media files to open, eliminating the need for streaming.

- **Host Management**: The host manages the session, controlling the media review process and facilitating communication among crew members.

## Getting Started

To get started with SwiftSync Media Reviewer, clone the repository and follow the instructions in the installation guide.

## Prerequisites

- Java Development Kit (JDK)
- IntelliJ IDEA 2023.3.2 or any other Java IDE

## Installation

1. Clone the repository: `git clone https://github.com/noahlake07/SwiftSync-Review/`
2. Open the project in your IDE.
3. Run `src/TestClient.java` to start a client.
4. Run `src/com/swiftsync/review/server/SSR_Server.java` to start the server.

## Usage

1. The host creates a new session using the `requestNewSession()` method in the `SessionClient` class.
2. Other crew members join the session using the `enterSession(String sessionID)` method in the `SessionClient` class.
3. The host controls what media everyone is looking at live.
4. Crew members communicate and make decisions using the built-in messaging feature.

## Contributing

Contributions are welcome. Please open an issue to discuss your ideas or submit a pull request.

## License

This project is licensed under the MIT License. See the `LICENSE` file for details.
