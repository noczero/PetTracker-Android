package com.zero.pettracker.ui.outdoor;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class OutdoorViewModel extends ViewModel {
    private MutableLiveData<String> mText;

    public OutdoorViewModel(){
        mText = new MutableLiveData<>();
        mText.setValue("This is outdoor fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }

}
