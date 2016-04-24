package pro.thor.yaartist;

import android.os.AsyncTask;

import java.util.ArrayList;

public abstract class NetworkWorkerTask extends AsyncTask<ArrayList, Void, ArrayList>
{
    NetworkInfoLoader networkInfoLoader;

    //defines what do after file is loaded or error received
    public abstract void doSomethingAfterExecute(ArrayList result);

    //class constructor
    public NetworkWorkerTask(NetworkInfoLoader networkInfoLoader)
    {
        this.networkInfoLoader = networkInfoLoader;
    }

    // Load image in background.
    @Override
    protected ArrayList doInBackground(ArrayList... params) {
        ArrayList data = params[0];
        return downloadFile(data);
    }

    //what to do is defined in particular implementation
    @Override
    protected void onPostExecute(ArrayList result)
    {
        doSomethingAfterExecute(result);
    }

    //actual file downloading
    private ArrayList downloadFile(ArrayList data)
    {
        String networkAddressData = (String)data.get(0);
        String localAddressData = (String)data.get(1);
        if(networkInfoLoader.downloadSomething(networkAddressData,localAddressData))data.set(0,true);
        else data.set(0,false);
        return data;
    }
}
