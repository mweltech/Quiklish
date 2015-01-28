package com.example.maw.quiklish;

/**
 * Created by maw on 2/01/2015.
 */
import it.sauronsoftware.ftp4j.FTPAbortedException;
import it.sauronsoftware.ftp4j.FTPClient;
import it.sauronsoftware.ftp4j.FTPDataTransferException;
import it.sauronsoftware.ftp4j.FTPException;
import it.sauronsoftware.ftp4j.FTPIllegalReplyException;

import java.io.*;
import java.util.List;

import android.os.AsyncTask;
import android.widget.*;

public class FTPDownload extends AsyncTask<String, Integer, String>{
    DownloadListener alldone_listener;
    File private_path;
    TextView status_update;

     @Override
    protected void onPreExecute() {
        // do nothing
        //status_update.setText("Starting...");
    };

    //@Override
    protected String doInBackground(String... url) {

        android.os.Debug.waitForDebugger();

        String response="";
        String host       = url[0];
        String remoteDir  = url[1];
        //String remoteFile = url[2];
        Integer i=0;
        getFile(host,21,"ezyd9850","Ax3!YC9b",remoteDir,"ezydisplaydata.xml");   publishProgress(++i);
        List<DisplayItem> displayitems;
        File xmlFile = new File(private_path,"ezydisplaydata.xml");
        try {
            FileInputStream config_file = new FileInputStream(xmlFile.getAbsolutePath());
            DisplayListReader display_list_reader = new DisplayListReader();
            display_list_reader.readListFromFile(config_file);
            displayitems = display_list_reader.getList();
            config_file.close();
            for(DisplayItem displayitem : displayitems) {
                getFile(host,21,"ezyd9850","Ax3!YC9b",remoteDir,displayitem.file);
                publishProgress(++i);
            }
        }
        catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        //getFile(host,21,"ezyd9850","Ax3!YC9b",remoteDir,"EzyDisplay.jpeg");      publishProgress(++i);
        //getFile(host,21,"ezyd9850","Ax3!YC9b",remoteDir,"Handsome.jpeg");        publishProgress(++i);
        //getFile(host,21,"ezyd9850","Ax3!YC9b",remoteDir,"Restaurant.jpeg");      publishProgress(++i);
        //getFile(host,21,"ezyd9850","Ax3!YC9b",remoteDir,"EZYDISPLAY.m4v");       publishProgress(++i);
        //getFile(host,21,"ezyd9850","Ax3!YC9b",remoteDir,"eventlist1.xml");       publishProgress(++i);
        response="Done";
        return response;
    }

    void getFile(String host, int port, String user, String pass, String remoteDir,String remoteFile) {
        FTPClient client = new FTPClient();
        try {
            String serverHostMessage[] = client.connect( host, port);
            client.login(user, pass);

            client.changeDirectory(remoteDir);
            client.setType(FTPClient.TYPE_BINARY);
            client.setPassive(true);

            File localFile = File.createTempFile("ezyd", ".tmp", private_path); //directoryEzyDisplay);
            client.download(remoteFile, localFile );
            client.disconnect(true);

            File successFile = new File(private_path.getPath(),remoteFile);
            successFile.delete();
            localFile.renameTo(successFile);
        } catch (IllegalStateException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (FTPIllegalReplyException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (FTPException e) {
            // TODO Auto-generated catch block
            e.getCode();
            e.getMessage();
            e.printStackTrace();
        } catch (FTPDataTransferException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (FTPAbortedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Override
    protected void onProgressUpdate(Integer... progress) {
        status_update.setText("Downloaded "+progress[0].toString()+" files");
    }

    @Override
    protected void onPostExecute(String result) {
        // TODO Auto-generated method stub
        //status_update.setText(result); //"Finished");
        // Update your UI here
        alldone_listener.isFinished();
        return;
    }



    public void setPath(File filesDir) {
        // TODO Auto-generated method stub
        private_path=filesDir;
        if (!private_path.mkdirs()) {
            //error

        }

    }

    public void setStatusDisplay(TextView status) {
        status_update=status;
    }

    public void setDownloadListener(DownloadListener lstnr) {
        alldone_listener = lstnr;
    }

}
