# Java 2D Game Template

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Java](https://img.shields.io/badge/Java-17%2B-orange)](https://www.oracle.com/java/technologies/javase/jdk17-archive-downloads.html)
[![Gradle](https://img.shields.io/badge/Gradle-7.4%2B-blue)](https://gradle.org/)
[![Status](https://img.shields.io/badge/Status-Development-green)](https://github.com/your-username/2d)

A lightweight, performant Java 2D game engine template using Swing/AWT for rendering. Features a game state management system, optimized rendering pipeline, and modular entity framework.

![Game Screenshot](docs/screenshots/screenshot.png)

## Features

- ✅ Optimized game loop with consistent frame rate
- ✅ State-based game architecture (Menu, Play, Pause)
- ✅ Double-buffered rendering with hardware acceleration
- ✅ Sprite animation system with flexible frame timing
- ✅ Input handling for keyboard and mouse
- ✅ Resource loading and caching system
- ✅ Entity-based game object implementation
- ✅ Collision detection system
- ✅ UI components system (buttons, etc.)

## Project Structure

```
app/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/vincentramdhanie/twod/game/
│   │   │       ├── core/         # Core game engine components
│   │   │       ├── entity/       # Game entities (player, enemies, etc.)
│   │   │       ├── graphics/     # Rendering and animation utilities
│   │   │       ├── input/        # Input handling
│   │   │       ├── state/        # Game states
│   │   │       ├── ui/           # UI components
│   │   │       └── utils/        # Utility classes
│   │   └── resources/            # Game assets (images, sounds, etc.)
│   └── test/                     # Unit tests
└── build.gradle                  # Gradle build configuration
```

## Prerequisites

- Java JDK 17 or higher
- Gradle 7.4 or higher (or use the included Gradle wrapper)

## Installation and Setup

1. Clone the repository:
   ```bash
   git clone https://github.com/your-username/2d.git
   cd 2d
   ```

2. Build the project:
   ```bash
   ./gradlew build
   ```

3. Run the game:
   ```bash
   ./gradlew run
   ```

## Development Guide

### Creating a New Entity

Extend the `Entity` class to create a new game object:

```java
public class MyEntity extends Entity {
    public MyEntity(float x, float y, int width, int height) {
        super(x, y, width, height);
    }
    
    @Override
    public void update() {
        // Update entity logic here
    }
    
    @Override
    public void render(Graphics2D g) {
        // Rendering code here
    }
}
```

### Adding a New Game State

Extend the `GameState` class to create a new game state:

```java
public class MyGameState extends GameState {
    public MyGameState(GameStateManager gsm) {
        super(gsm);
    }
    
    @Override
    public void init() {
        // Initialize state
    }
    
    @Override
    public void update() {
        // Update state logic
    }
    
    @Override
    public void render(Graphics2D g) {
        // Render state
    }
    
    // Implement other required methods...
}
```

### Customizing the Menu

Modify the `MenuState` class to customize the game menu:

```java
// Add new buttons or change existing ones
buttons.add(new Button(x, y, width, height, "New Button"));
```

## Performance Optimization

The template includes several optimizations:

- Hardware-accelerated rendering via OpenGL
- Static state rendering for non-animated screens
- Resource caching to prevent reloading assets
- Double buffering to eliminate screen tearing
- Sprite sheet handling for efficient animation

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add some amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## License

This project is licensed under the MIT License - see below for details:

```
MIT License

Copyright (c) 2023 Vincent Ramdhanie

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```

## Acknowledgments

- Inspired by various Java game development tutorials and frameworks
- Special thanks to all contributors and the Java game development community 