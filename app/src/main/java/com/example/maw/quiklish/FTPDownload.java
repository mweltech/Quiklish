package com.example.maw.quiklish;

/**
 * Created by maw on 2/01/2015.
 */
import it.sauronsoftware.ftp4j.FTPAbortedException;
import it.sauronsoftware.ftp4j.FTPClient;
import it.sauronsoftware.ftp4j.FTPDataTransferException;
import it.sauronsoftware.ftp4j.FTPException;
import it.sauronsoftware.ftp4j.FTPFile;
import it.sauronsoftware.ftp4j.FTPIllegalReplyException;
import it.sauronsoftware.ftp4j.FTPListParseException;

import java.io.*;
import java.util.List;

import android.os.AsyncTask;
import android.provider.ContactsContract;
import android.widget.*;

public class FTPDownload extends AsyncTask<String, Integer, String>{
    DownloadListener alldone_listener;
    TextView status_update;

     @Override
    protected void onPreExecute() {
        // do nothing
        //status_update.setText("Starting...");
    };

    //@Override
    protected String doInBackground(String... url) {

        //android.os.Debug.waitForDebugger();

        String response="";
        String host       = url[0];
        String remoteDir  = url[1];
        String localDir   = url[2];
        String username   = url[3];
        String password   = url[4];
        String galleryDir = url[5];
        Integer i=0;
        //getFile(host,21,"ezyd9850","Ax3!YC9b",remoteDir,"ezydisplaydata.xml",localDir);   publishProgress(++i);
        getFile(host,21,username,password,remoteDir,"ezydisplaydata.xml",localDir);   publishProgress(++i);
        List<DisplayItem> displayitems;
        File xmlFile = new File(localDir,"ezydisplaydata.xml");
        try {
            FileInputStream config_file = new FileInputStream(xmlFile.getAbsolutePath());
            DisplayListReader display_list_reader = new DisplayListReader();
            display_list_reader.readListFromFile(config_file);
            displayitems = display_list_reader.getList();
            config_file.close();
            for(DisplayItem displayitem : displayitems) {
                //getFile(host,21,"ezyd9850","Ax3!YC9b",remoteDir,displayitem.file,localDir);
                getFile(host,21,username,password,remoteDir,displayitem.file,localDir);
                publishProgress(++i);
            }
            getGalleryFiles(host,21,username,password,remoteDir+"Gallery",galleryDir);
        }
        catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        response="Done";
        return response;
    }

    boolean getGalleryFiles(String host, int port, String user, String pass, String remoteDir,String localDir) {
        boolean success=false;
        FTPClient client = new FTPClient();
        try {
            String serverHostMessage[] = client.connect(host, port);
            client.login(user, pass);
            client.changeDirectory(remoteDir);
            client.setType(FTPClient.TYPE_BINARY);
            client.setPassive(true);
            File localDirectory = new File(localDir);
            localDirectory.mkdirs();
            localDirectory.setReadable(true, false);
            FTPFile[] list = client.list();
            client.disconnect(true);
            success=true;
            for (int i=0;i<list.length;i++) {
                getFile(host,port,user,pass,remoteDir,list[i].getName(),localDir);
                publishProgress(i+1);
            }
        } catch (FTPListParseException e) {
            e.printStackTrace();
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
        return success;
    }

    boolean getFile(String host, int port, String user, String pass, String remoteDir,String remoteFile,String localDir) {
        boolean success=false;
        FTPClient client = new FTPClient();
        try {
            String serverHostMessage[] = client.connect( host, port);
            client.login(user, pass);
            client.changeDirectory(remoteDir);
            client.setType(FTPClient.TYPE_BINARY);
            client.setPassive(true);
            File localDirectory = new File(localDir);
            localDirectory.mkdirs();
            localDirectory.setReadable(true, false);
            File localFile = File.createTempFile("ezyd", ".tmp", localDirectory); //directoryEzyDisplay);
            client.download(remoteFile, localFile);
            client.disconnect(true);

            File successFile = new File(localDirectory,remoteFile);
            successFile.delete();
            success = localFile.renameTo(successFile);
            successFile.setReadable(true,false);
            successFile.setWritable(true,false);
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
        return success;
    }

    @Override
    protected void onProgressUpdate(Integer... progress) {
        status_update.append("Downloaded "+progress[0].toString()+" files\n");

    }

    @Override
    protected void onPostExecute(String result) {
        // TODO Auto-generated method stub
        // Update your UI here
        alldone_listener.isFinished();
        return;
    }

    public void setStatusDisplay(TextView status) {
        status_update=status;
    }

    public void setDownloadListener(DownloadListener lstnr) {
        alldone_listener = lstnr;
    }

}
