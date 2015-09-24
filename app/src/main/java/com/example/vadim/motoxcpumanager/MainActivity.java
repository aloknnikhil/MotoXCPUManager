package com.example.vadim.motoxcpumanager;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.DataOutputStream;
import java.util.regex.Pattern;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends Activity {
    TextView textAvProc, textNumOfCpu, textMsg;
    Button btnReadCpuFreq;
    Button btnEnableAllCpu;
    Button btnDisable2Cpu;
    Button btnDisable3Cpu;
    Button btnDisable4Cpu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textAvProc = (TextView)findViewById(R.id.avproc);
        textNumOfCpu = (TextView)findViewById(R.id.numofcpu);
        textMsg = (TextView)findViewById(R.id.msg);
        btnReadCpuFreq = (Button)findViewById(R.id.readfreq);
        btnEnableAllCpu = (Button)findViewById(R.id.enableallcpu);
        btnDisable3Cpu = (Button)findViewById(R.id.disable3cpu);
        btnDisable2Cpu = (Button)findViewById(R.id.disable2cpu);
        btnDisable4Cpu = (Button)findViewById(R.id.disable4cpu);

        Runtime runtime = Runtime.getRuntime();
        int availableProcessors = runtime.availableProcessors();
        textAvProc.setText("availableProcessors = " + availableProcessors);

        btnReadCpuFreq.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                readCpuFreqNow();
            }
        });
        btnEnableAllCpu.setOnClickListener(new OnClickListener(){

            @Override
            public void onClick(View v) {
                EnableAllCPU();
                readCpuFreqNow();
            }});
        btnDisable3Cpu.setOnClickListener(new OnClickListener(){

            @Override
            public void onClick(View v) {
                DisableNCPU(3);
                readCpuFreqNow();
            }});
        btnDisable2Cpu.setOnClickListener(new OnClickListener(){

            @Override
            public void onClick(View v) {
                DisableNCPU(2);
                readCpuFreqNow();
            }});
        btnDisable4Cpu.setOnClickListener(new OnClickListener(){

            @Override
            public void onClick(View v) {
                DisableNCPU(4);
                readCpuFreqNow();
            }});

        readCpuFreqNow();
    }

    private void readCpuFreqNow(){
        File[] cpuFiles = getCPUs();
        textNumOfCpu.setText("number of cpu: " + cpuFiles.length + ". CPUs frequency:");

        String strFileList = "";
        for(int i=0; i<cpuFiles.length; i++){

            String path_scaling_cur_freq =
                    cpuFiles[i].getAbsolutePath()+"/cpufreq/scaling_cur_freq";

            String scaling_cur_freq = cmdCat(path_scaling_cur_freq);

            strFileList +=
                    i + ": " + scaling_cur_freq + "\n";
        }

        textMsg.setText(strFileList);
    }

    private void EnableAllCPU(){
        Process p;
        try {
            // Preform su to get root privileges
            p = Runtime.getRuntime().exec("su");

            // Attempt to write a file to a root-only
            DataOutputStream os = new DataOutputStream(p.getOutputStream());
            os.writeBytes("echo 1 > /sys/devices/system/cpu/cpu2/online\n");
            os.writeBytes("echo 1 > /sys/devices/system/cpu/cpu3/online\n");
            os.writeBytes("echo 1 > /sys/devices/system/cpu/cpu4/online\n");
            os.writeBytes("echo 1 > /sys/devices/system/cpu/cpu5/online\n");

            // Close the terminal
            os.writeBytes("exit\n");
            os.flush();
            try {
                p.waitFor();
            } catch (InterruptedException e) {
            }
        } catch (IOException e) {
        }

    }

    private void DisableNCPU(int N){
        Process p;
        try {
            // Preform su to get root privileges
            p = Runtime.getRuntime().exec("su");

            // Attempt to write a file to a root-only
            DataOutputStream os = new DataOutputStream(p.getOutputStream());
            for (int i=0; i<N; i++){
                os.writeBytes("echo 0 > /sys/devices/system/cpu/cpu"+(5-i)+"/online\n");
            }

            // Close the terminal
            os.writeBytes("exit\n");
            os.flush();
            try {
                p.waitFor();
            } catch (InterruptedException e) {
            }
        } catch (IOException e) {
        }

    }


    //run Linux command
    //$ cat f
    private String cmdCat(String f){

        String[] command = {"cat", f};
        StringBuilder cmdReturn = new StringBuilder();

        try {
            ProcessBuilder processBuilder = new ProcessBuilder(command);
            Process process = processBuilder.start();

            InputStream inputStream = process.getInputStream();
            int c;

            while ((c = inputStream.read()) != -1) {
                cmdReturn.append((char) c);
            }

            return cmdReturn.toString();

        } catch (IOException e) {
            e.printStackTrace();
            return "Something Wrong";
        }

    }
    /*
      * Get file list of the pattern
      * /sys/devices/system/cpu/cpu[0..9]
      */
    private File[] getCPUs(){

        class CpuFilter implements FileFilter {
            @Override
            public boolean accept(File pathname) {
                if(Pattern.matches("cpu[0-9]+", pathname.getName())) {
                    return true;
                }
                return false;
            }
        }

        File dir = new File("/sys/devices/system/cpu/");
        File[] files = dir.listFiles(new CpuFilter());
        return files;
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

}
