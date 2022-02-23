# CS4900_Indiv_Mini_Project
Android App with Image Classification
# Functionality
The app has two buttons. One to load an Image from the gallery, and the other to initiate the classification process to display the predicted class name of the image. The only other visible feature of the app is the ImageView that contains a generic image icon when now image has been selected
# Main Characteristics
The app build.Gradle file implemted the newest android versions. Main Activity contains the functions for the buttons and the classification process. Activity_main.xml contains the architecture for the UI. The assets folder contains the scripted PyTorch model. I used the quantized, optimized-for-mobile version of Resnet18. Finally, I had to create a java class containing a String array of the classmap labels.
# Main Resources
https://pytorch.org/tutorials/recipes/mobile_interpreter.html
https://www.youtube.com/watch?v=5Lxuu16_28o
