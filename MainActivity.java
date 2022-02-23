package com.example.house_indiv_mini_project;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import org.pytorch.IValue;
import org.pytorch.LiteModuleLoader;
import org.pytorch.Module;
import org.pytorch.Tensor;
import org.pytorch.torchvision.TensorImageUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Scanner;

public class MainActivity extends AppCompatActivity{

    private Bitmap bitmap = null;
    private Module module = null;
    private ImageView imageView = null;
    private Uri selectedImage = null;


    // Returns the string of the absolute path of the assets
    public static String assetFilePath(Context context, String assetName) throws IOException{
        File file = new File(context.getFilesDir(), assetName);
        if( file.exists() && file.length() > 0){
            return file.getAbsolutePath();
        }

        try (InputStream is = context.getAssets().open(assetName)){
            try(OutputStream os = new FileOutputStream(file)){
                byte[] buffer = new byte[4 * 1024];
                int read;
                while((read = is.read(buffer)) != -1){
                    os.write(buffer, 0, read);
                }
                os.flush();
            }
            return file.getAbsolutePath();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && data != null) {

            imageView = findViewById(R.id.imageView);
            try {
                //receive the data from selected image
                InputStream inputStream = getContentResolver().openInputStream(data.getData());
                //create a bitmap of that image and set it to the Bitmap variable we declared earlier
                bitmap = BitmapFactory.decodeStream(inputStream);
                //set the imageView bitmap to the selected image bitmap (possibly not needed)
                imageView.setImageBitmap(bitmap);
            }
            catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Read External Storage (Possibly not needed, also permission in "AndroidManifest.xml")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(new String[]
                    {android.Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
        }


        //try {
            // (possibly not needed)
            //bitmap = BitmapFactory.decodeStream(getAssets().open("kitten.jpg"));

            //loading serialized torchscript module from package into app android asset resnet18_scripted.py
            //app/src/module/assets/resnet18_scripted.py
            //module = Module.load(assetFilePath(this, "resnet18_scripted.py"));
        //module = Module.load("/Users/brishenhouse/AndroidStudioProjects/House_Indiv_Mini_Project/app/src/main/assets/resnet18_scripted.py");

        try {
            module = LiteModuleLoader.load(MainActivity.assetFilePath(getApplicationContext(), "resnet18_scripted_efficient.py"));        //} catch (IOException e) {
        } catch (IOException e) {
            e.printStackTrace();
        }
        //  Log.e("PTRDryRun", "Error Reading assets", e);
         //   finish();
       // }


        // (possibly not needed) showing Image on UI
        //ImageView imageView = findViewById(R.id.imageView);
        //imageView.setImageBitmap(bitmap);

        // determining the functionality of the gallery button
        Button gallery = findViewById(R.id.gallery);
        gallery.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, 3);
            }

        });

        // determining the functionality of the classify button
        Button classify = findViewById(R.id.classify);
        classify.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View view) {

                //Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                //Intent.setType("image/*");
                //startActivityForResult(intent, 3);

                //converting bitmap to Tensor
                final Tensor inputTensor = TensorImageUtils.bitmapToFloat32Tensor(bitmap,
                        TensorImageUtils.TORCHVISION_NORM_MEAN_RGB, TensorImageUtils.TORCHVISION_NORM_STD_RGB);

                // running the model
                final Tensor outputTensor = module.forward(IValue.from(inputTensor)).toTensor();

                // converting Tensor content as java float array
                final float[] scores = outputTensor.getDataAsFloatArray();

                //searching for the index with max score
                float maxScore = -Float.MAX_VALUE;
                int maxScoreIdx = -1;
                for(int i=0; i<scores.length; i++){
                    if(scores[i] > maxScore){
                        maxScore = scores[i];
                        maxScoreIdx = i;
                    }
                }

                //Getting the String of the class name from "imagenet_classes.txt"
//                String className = null;
//                try{
//                    Scanner scan = new Scanner("imagenet_classes.txt");
//                    scan.nextLine();
//                    className = scan.nextLine();
//
////                    int x=0;
////                    while (x <= maxScoreIdx){
////                        String nextLine = scan.nextLine();
////                        if(x==maxScoreIdx){
////                            className = nextLine;
////                        }
////                        x += 1;
////                    }
//                    scan.close();
//                } catch (Exception e) {
//                    e.printStackTrace();
//                    System.out.println("you suck");
//                }
                String className = com.example.house_indiv_mini_project.ImageNetClasses.IMAGENET_CLASSES[maxScoreIdx];

                // showing classname on UI
                TextView textView = findViewById(R.id.resultView);
                textView.setText(className);
            }
        });
    }
}
