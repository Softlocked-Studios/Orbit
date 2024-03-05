# Orbit Programming Language
Orbit is a multi-paradigm programming language that is interpreted and built on top of Java, designed for simplicity, versatility and performance. It offers a unique blend of features from imperative, functional, and object-oriented programming paradigms, making it suitable for a wide range of applications, from scripting to large-scale software development.

Orbit has a unique approach to it's syntax, offering a diverse range of options to accommodate different programming styles and preferences. For example, Orbit draws inspiration from languages like C, Lua, JavaScript, and Python. Whether you would like a traditional C-like syntax with braces and semicolons or embracing more expressive alternatives such as the usage of keywords like "then" and "end", Orbit has them both, and much more.

## Features
- Multi-Paradigm: Orbit supports imperative, functional, and object-oriented programming paradigms, allowing developers to choose the style that best suits their needs.
- Diverse Syntax: As already stated, developers have the freedom to write code in a manner that best suits their individual preferences.
- Dynamic Typing: Variables in Orbit are dynamically typed, allowing for flexibility and ease of use.
- Garbage Collection: Due to being built on Java, Orbit also employs automatic memory management through garbage collection.
- Extensible: Orbit additionally allows developers to directly load java libraries into it's programs.

## How to Run
The following should be used as arguments for when running the jar.
Example:
```sh
java -jar Orbit.jar opkg init
```

## Running the Orbit REPL
To run the Orbit REPL (Read-Eval-Print Loop):
```sh
orbit
```

### Using the Orbit Package Manager (opkg)
To utilize the Orbit Package Manager (opkg) for managing packages and projects:
1. Initialization: (Will create a `metadata.yml` file in the working directory)
```sh
opkg init
```
2. Running: (Will directly run the code from the entrypoint)
```sh
opkg run
```

### Modules
As of speaking, there are no repositories for Orbit. If you wish to use any sort of dependencies, you will need to manually add them under `projectDir/modules/{name}` and must contain a `metadata.yml` file. (In the same format as when running `opkg init`)
