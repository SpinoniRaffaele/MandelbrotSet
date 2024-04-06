# Mandelbrot Set Visualizer

This is a simple Mandelbrot Set visualizer program written in Java using the LWJGL library. 
The Mandelbrot Set is a set of complex numbers that is defined by iterating a function on a complex number 
and checking if the result diverges. 
The Mandelbrot Set is a fractal, meaning that it has self-similarity at different scales.
This program allows you to zoom in on the Mandelbrot Set and explore its intricate patterns.

The project is heavily inspired by the C program written by Patrick Gono:
https://physicspython.wordpress.com/2020/02/16/visualizing-the-mandelbrot-set-using-opengl-part-1/

![Look and feel of the program](src/main/resources/images/mandelbrot-presentation.gif)

## How to run the program
A local installation of Java 17 or higher is required to run the program.

After downloading the repository, build the project using the following command:
```
mvn package
```

Then, run the program using the following command:
```
java -jar target/MandelbrotSet-1.0-jar-with-dependencies.jar
```
