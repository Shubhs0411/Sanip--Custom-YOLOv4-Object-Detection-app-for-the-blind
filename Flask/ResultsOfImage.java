package com.example.flask_to_apk2;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.FileUtils;
import android.widget.Button;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;

public class ResultsOfImage extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_results_of_image);
        File dir = new File(Environment.getExternalStorageDirectory() + "/dcim/wheatclassifier");
        deleteRecursive(dir);
        dir.mkdirs();


        int numgrainint = 0, missclassint = 0;
        double grade0 = 0.0, grade1 = 0.0, grade2 = 0.0, totalgrade= 0.0, misclassper = 0.0;
        String breedstr = "No";


        Intent i = getIntent();
        String res1 = i.getStringExtra("RESU_OF_JSON");

        JSONObject Jobject = null;
        try {


            Jobject = new JSONObject(res1);
            numgrainint = Jobject.getInt("Total Grains");
            breedstr = Jobject.getString("Breed of Sample");
            missclassint = Jobject.getInt("Missclassified Grains");
            misclassper = (((double)(missclassint))*100)/((double)(numgrainint));
            grade0 = Jobject.getDouble("Grade 0");
            grade1 = Jobject.getDouble("Grade 1");
            grade2 = Jobject.getDouble("Grade 2");
            totalgrade = Jobject.getDouble("Grade of Sample");


        } catch (JSONException e) {
            e.printStackTrace();
        }



        TextView numgrains1, breed1, missclass1, grade0v, grade1v, grade2v, gradetv;

        numgrains1 = findViewById(R.id.numgrains);
        numgrains1.setText(Integer.toString(numgrainint));

        missclass1 = findViewById(R.id.missclass);
        missclass1.setText(Double.toString(misclassper));

        breed1 = findViewById(R.id.breedimg);
        breed1.setText(breedstr);

        grade0v = findViewById(R.id.grade0t);
        grade0v.setText(Double.toString(grade0));

        grade1v = findViewById(R.id.grade1t);
        grade1v.setText(Double.toString(grade1));

        grade2v = findViewById(R.id.grade2t);
        grade2v.setText(Double.toString(grade2));

        gradetv = findViewById(R.id.gradett);
        gradetv.setText(Double.toString(totalgrade));




    }
    public void goback(android.view.View v){
        Intent intent = new Intent(ResultsOfImage.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }



    void deleteRecursive(File fileOrDirectory) {
        if (fileOrDirectory.isDirectory())
            for (File child : fileOrDirectory.listFiles())
                deleteRecursive(child);

        fileOrDirectory.delete();
    }



    /*
    bt1.setOnClickListener(new OnClickListener(){

        private void onClick(){
            Intent intent = new Intent(currentActivity.this, Main.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        }
    });

     */
}
