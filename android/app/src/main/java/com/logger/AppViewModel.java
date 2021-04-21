package com.logger;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelStoreOwner;

import com.logger.model.Match;

import java.util.List;

public class AppViewModel extends ViewModel {

    public static AppViewModel getInstance(@NonNull ViewModelStoreOwner owner) {
        ViewModelProvider provider = new ViewModelProvider(owner);
        return provider.get(AppViewModel.class);
    }

    private MutableLiveData<String> url;
    private MutableLiveData<String> mask;
    private MutableLiveData<List<String>> urlHistory;
    private MutableLiveData<List<String>> maskHistory;
    private MutableLiveData<List<Match>> matches;
    private MutableLiveData<Integer> progress;
    private MutableLiveData<Boolean> selectAll;

    public LiveData<String> getUrl() {
        if (url == null){
            url = new MutableLiveData<>();
        }
        return url;
    }
    public LiveData<String> getMask() {
        if (mask == null){
            mask = new MutableLiveData<>();
        }
        return mask;
    }
    public LiveData<List<String>> getUrlHistory() {
        if (urlHistory == null) {
            urlHistory = new MutableLiveData<>();
            App.dbWork(db -> {
                List<String> items = db.getUrlHistory();
                urlHistory.postValue(items);
            });
        }
        return urlHistory;
    }
    public LiveData<List<String>> getMaskHistory() {
        if (maskHistory == null) {
            maskHistory = new MutableLiveData<>();
            App.dbWork(db -> {
                List<String> items = db.getMaskHistory();
                maskHistory.postValue(items);
            });
        }
        return maskHistory;
    }
    public LiveData<List<Match>> getMatches() {
        if (matches == null) {
            matches = new MutableLiveData<>();
        }
        return matches;
    }
    public LiveData<Integer> getProgress() {
        if (progress == null) {
            progress = new MutableLiveData<>(0);
        }
        return progress;
    }
    public LiveData<Boolean> getSelectAll() {
        if (selectAll == null) {
            selectAll = new MutableLiveData<>(true);
        }
        return selectAll;
    }

    public void setUrl(String value) {
        url.setValue(value);
    }
    public void setMask(String value) {
        mask.setValue(value);
    }
    public void clearMaskHistory() {
        App.dbWork(db -> {
            db.clearMaskHistory();
            List<String> items = db.getMaskHistory();
            maskHistory.postValue(items);
        });
    }
    public void switchSelectAll() {
        Boolean checked = selectAll.getValue();
        if (checked != null) {
            selectAll.setValue(!checked);
        }
    }
    public void startSearch(String url, String mask) {

        this.url.setValue(url);
        this.mask.setValue(mask);

        App.dbWork(db -> {
            db.saveUrl(url);
            db.saveMask(mask);
            urlHistory.postValue(db.getUrlHistory());
            maskHistory.postValue(db.getMaskHistory());
        });


//        if (task == null || task.isComplete()) {
//            task = DownloadTask.getOrCreate(model.url, model.mask);
//            task.execute();
//        }
//
//        task.addListener(new DownloadTask.Listener() {
//            @Override
//            public void onMatch(List<String> items) {
//                if (items.isEmpty()){
//                    return;
//                }
//
//                ArrayList<Match> result = new ArrayList<>(items.size());
//                for(String item : items) {
//                    result.add(new Match(item, false));
//                }
//
//                requireActivity().runOnUiThread(() -> {
//                    adapter.addAll(result);
//                    adapter.notifyDataSetChanged();
//                });
//            }
//            @Override
//            public void onProgress(int progress) {
//                requireActivity().runOnUiThread(() -> {
//                    progressBar.setProgress(progress);
//                });
//            }
//            @Override
//            public void onComplete() {
//            }
//            @Override
//            public void onCancel() { }
//            @Override
//            public void onFail() { }
//        });
    }

    public void resumeSearch() {

    }
    public void pauseSearch() {

    }
    public void cancelSearch() {

    }
}
