package com.example.fimae.viewmodels;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.databinding.BaseObservable;
import androidx.databinding.Bindable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.fimae.BR;
import com.example.fimae.models.Fimaers;
import com.example.fimae.repository.FimaerRepository;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

public class ProfileViewModel  extends ViewModel {

    private MutableLiveData<Fimaers> user;

    public MutableLiveData<Fimaers> getUser() {
        return user;
    }

    private FimaerRepository userRepo;


    public ProfileViewModel(){

        userRepo = FimaerRepository.getInstance();
        fetchUser();
    }

    public MutableLiveData<Fimaers> fetchUser()
    {
        Log.i("PROFILEVM", "fetchUser: ");
        if(user == null)
        {
            user = userRepo.getCurrentUser();
        }
        return user;
    }

    public void setName(String value)
    {
        Fimaers tset = user.getValue();
        tset.setName(value);
        user.setValue(tset);
    }


    public Task<Void> updateUser()
    {
        return userRepo.updateProfile(user.getValue());
    }
}