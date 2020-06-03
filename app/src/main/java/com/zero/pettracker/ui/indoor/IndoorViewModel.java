package com.zero.pettracker.ui.indoor;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class IndoorViewModel extends ViewModel {

    private MutableLiveData<String> mText;
    private MutableLiveData<String> statusNetwork;

    public IndoorViewModel(){
        mText = new MutableLiveData<>();
        mText.setValue("This is indoor fragment");

        statusNetwork = new MutableLiveData<>();
        statusNetwork.setValue("Connected...");
    }

    public LiveData<String> getText() {
        return mText;
    }

    public LiveData<String> getStatusNetwork(){
        return statusNetwork;
    }
}
