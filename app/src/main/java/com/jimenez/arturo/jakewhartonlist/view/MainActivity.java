package com.jimenez.arturo.jakewhartonlist.view;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Process;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.jimenez.arturo.jakewhartonlist.R;
import com.jimenez.arturo.jakewhartonlist.models.RepoModel;
import com.jimenez.arturo.jakewhartonlist.services.GitHubService;
import com.jimenez.arturo.jakewhartonlist.services.ServiceGenerator;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.android.schedulers.HandlerScheduler;
import rx.functions.Func0;


public class MainActivity extends ActionBarActivity {

    @Bind(R.id.repoList) ListView reposListView;

    private Handler backgroundHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        BackgroundThread backgroundThread = new BackgroundThread();
        backgroundThread.start();
        backgroundHandler = new Handler(backgroundThread.getLooper());

        loadRepos();
    }

    private void loadRepos() {
        reposObservable()
                .subscribeOn(HandlerScheduler.from(backgroundHandler))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<List<RepoModel>>() {
                    List<RepoModel> repos = new ArrayList<>();

                    @Override
                    public void onCompleted() {
                        setupRepoListView(repos);
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e("Ops! Error: ", "Rx Did it again!", e);
                    }

                    @Override
                    public void onNext(List<RepoModel> repoModels) {
                        repos.addAll(repoModels);
                    }
                });
    }

    private void setupRepoListView(final List<RepoModel> repos) {
        List<String> repoNames = loadRepoNames(repos);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getBaseContext(),
                android.R.layout.simple_list_item_1, android.R.id.text1, repoNames);
        reposListView.setAdapter(adapter);
        reposListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                goToRepoWebsite(repos, position);
            }
        });
    }

    private List<String> loadRepoNames(List<RepoModel> repos) {
        List<String> repoNames = new ArrayList<>();
        for (RepoModel repoModel : repos) {
            repoNames.add(repoModel.getName());
        }
        return repoNames;
    }

    private void goToRepoWebsite(List<RepoModel> repos, int position) {
        RepoModel repoModel    = repos.get(position);
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(repoModel.getHtml_url()));
        startActivity(i);
    }

    private static Observable<List<RepoModel>> reposObservable() {
        return Observable.defer(new Func0<Observable<List<RepoModel>>>() {
            @Override
            public Observable<List<RepoModel>> call() {
                String baseUrl = "https://api.github.com";
                String username = "JakeWharton";
                GitHubService gitHubService = ServiceGenerator.createService(GitHubService.class, baseUrl);
                return Observable.just(gitHubService.listRepos(username));
            }
        });
    }

    private static class BackgroundThread extends HandlerThread {
        BackgroundThread() {
            super("SchedulerSample-BackgroundThread", Process.THREAD_PRIORITY_BACKGROUND);
        }
    }

}
