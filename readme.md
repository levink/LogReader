## LogReader

This is small regex engine, written on C++ and used in Android application. It is test task from one of my employer, which i had successfully done.

Program download ANSI-text file by URL address, parse it with regular expression, count and print matched lines.<br>
Also it remembers search history, allow to select and copy to clipboard matched lines

#### Regex

- '*' - any symbol sequence or nothing
- '?' - any one symbol
- A-Z, a-z, 0-9, etc... any ANSI symbols

Example: regex "?asd*" find matches on "wasd123" string.

#### Structure
Project contains two parts: Java application for Android and C++ program for Windows. Android app asynchronously load resources and transfer data blocks through JNI to C++ engine for parsing. Win-part contains unit tests for C++ engine. Such split allows fast dev-test cycle for native part.

#### Coding time
I've spent around 40 hours for write it. No magic. Only this approach works: think-code-test-clean-think... =)

