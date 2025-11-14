package com.openclassrooms.tourguide.service;

import com.openclassrooms.tourguide.user.User;
import gpsUtil.GpsUtil;
import gpsUtil.location.VisitedLocation;
import rewardCentral.RewardCentral;

import java.util.concurrent.CompletableFuture;

public class VisitedLocationMultiThread extends Thread {

    private final GpsUtil gpsUtil = new GpsUtil();

    private final RewardCentral rewardCentral = new RewardCentral();

    private final RewardsService rewardsService = new RewardsService(gpsUtil, rewardCentral);

    private final User user;

    private VisitedLocation visitedLocation;

    public VisitedLocationMultiThread(User user) {
        this.user = user;
    }

    @Override
    public void run() {
        trackUserLocation();
    }

    private void trackUserLocation() {

        final var userLocation = gpsUtil.getUserLocation(user.getUserId());

        user.addToVisitedLocations(userLocation);

        calculateRewards(user);

        visitedLocation = userLocation;
    }
    //### `CompletableFuture.allOf();

    private void calculateRewards(User user) {
        CompletableFuture.runAsync(() -> {
            rewardsService.calculateRewards(user);
        });
    }

    private CompletableFuture<VisitedLocation> getVisitedLocation(User user) {
        return CompletableFuture.supplyAsync(() -> gpsUtil.getUserLocation(user.getUserId()));
    }

    public User getUser() {
        return user;
    }

    public VisitedLocation getVisitedLocation() {
        return visitedLocation;
    }
}
