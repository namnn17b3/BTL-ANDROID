package com.masternam.ptitmanagestudentscore.listener;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public abstract class PaginationScrollListener extends RecyclerView.OnScrollListener {
    private LinearLayoutManager linearLayoutManager;

    public PaginationScrollListener(LinearLayoutManager linearLayoutManager) {
        this.linearLayoutManager = linearLayoutManager;
    }

    @Override
    public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
        super.onScrolled(recyclerView, dx, dy);

        int visibleItemCount = linearLayoutManager.getChildCount();
        int totalItemCount = linearLayoutManager.getItemCount();
        int firstVisibleItemPosition = linearLayoutManager.findFirstVisibleItemPosition();

        if (isLoading() || isLastPage()) {
            return;
        }

        if (firstVisibleItemPosition >= 0 && visibleItemCount + firstVisibleItemPosition >= totalItemCount) {
            System.out.println(">>> firstVisibleItemPosition: "+firstVisibleItemPosition);
            System.out.println(">>> visibleItemCount: "+visibleItemCount);
            System.out.println(">>> totalItemCount: "+totalItemCount);
            loadMoreItems();
        }
    }

    public abstract void loadMoreItems();
    public abstract boolean isLoading();
    public abstract boolean isLastPage();
}
