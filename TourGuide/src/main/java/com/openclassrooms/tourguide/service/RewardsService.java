package com.openclassrooms.tourguide.service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.springframework.stereotype.Service;

import gpsUtil.GpsUtil;
import gpsUtil.location.Attraction;
import gpsUtil.location.Location;
import gpsUtil.location.VisitedLocation;
import rewardCentral.RewardCentral;
import com.openclassrooms.tourguide.user.User;
import com.openclassrooms.tourguide.user.UserReward;

@Service
public class RewardsService {
    private static final double STATUTE_MILES_PER_NAUTICAL_MILE = 1.15077945;

	// proximity in miles
    private int defaultProximityBuffer = 10;
	private int proximityBuffer = defaultProximityBuffer;
	private int attractionProximityRange = 200;
	private final GpsUtil gpsUtil;
	private final RewardCentral rewardsCentral;
	private final ExecutorService executor = Executors.newFixedThreadPool(320);
	
	public RewardsService(GpsUtil gpsUtil, RewardCentral rewardCentral) {
		this.rewardsCentral = rewardCentral;
		this.gpsUtil = gpsUtil;
	}
	
	public void setProximityBuffer(int proximityBuffer) {
		this.proximityBuffer = proximityBuffer;
	}
	
	public void setDefaultProximityBuffer() {
		proximityBuffer = defaultProximityBuffer;
	}

	public CompletableFuture<Void> calculateRewards(User user) {
		if (user == null) {
			throw new NullPointerException("user cannot be null");
		}

		return CompletableFuture.runAsync(() -> {
			List<VisitedLocation> userLocations = List.copyOf(user.getVisitedLocations());
			List<Attraction> attractions = List.copyOf(gpsUtil.getAttractions());

			List<UserReward> existingUserRewards = List.copyOf(user.getUserRewards());
			List<UserReward> userRewardsToAdd = new ArrayList<>();

			for(VisitedLocation visitedLocation : userLocations) {
				for(Attraction attraction : attractions) {
					if(existingUserRewards.stream().filter(r -> r.attraction.attractionName.equals(attraction.attractionName)).count() == 0) {
						if(nearAttraction(visitedLocation, attraction)) {
							userRewardsToAdd.add(new UserReward(visitedLocation, attraction, getRewardPoints(attraction, user)));
						}
					}
				}
			}

			if (!userRewardsToAdd.isEmpty()) {
				userRewardsToAdd.forEach(user::addUserReward);
			}
		}, executor);
	}
	
	public boolean isWithinAttractionProximity(Attraction attraction, Location location) {
		return getDistance(attraction, location) > attractionProximityRange ? false : true;
	}
	
	private boolean nearAttraction(VisitedLocation visitedLocation, Attraction attraction) {
		return getDistance(attraction, visitedLocation.location) > proximityBuffer ? false : true;
	}
	
	public int getRewardPoints(Attraction attraction, User user) {
		return rewardsCentral.getAttractionRewardPoints(attraction.attractionId, user.getUserId());
	}
//
//	public CompletableFuture<Integer> getRewardPoints(Attraction attraction, User user) {
//		return CompletableFuture.supplyAsync(() -> rewardsCentral
//				.getAttractionRewardPoints(attraction.attractionId, user.getUserId()), executor);
//	}
	
	public double getDistance(Location loc1, Location loc2) {
        double lat1 = Math.toRadians(loc1.latitude);
        double lon1 = Math.toRadians(loc1.longitude);
        double lat2 = Math.toRadians(loc2.latitude);
        double lon2 = Math.toRadians(loc2.longitude);

        double angle = Math.acos(Math.sin(lat1) * Math.sin(lat2)
                               + Math.cos(lat1) * Math.cos(lat2) * Math.cos(lon1 - lon2));

        double nauticalMiles = 60 * Math.toDegrees(angle);
        double statuteMiles = STATUTE_MILES_PER_NAUTICAL_MILE * nauticalMiles;
        return statuteMiles;
	}

	public void shutdownExecutorService() {
		executor.shutdown();

		try {
			// Wait 10 seconds for the tasks to terminate
			if (!executor.awaitTermination(10, TimeUnit.SECONDS)) {
				// Cancel currently executing tasks
				executor.shutdownNow();

				// Wait 60 seconds for tasks to respond
				if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
					System.err.println("Pool did not terminate");
				}
			}
		} catch (InterruptedException ex) {
			// Cancel if the current thread was interrupted
			executor.shutdownNow();
			// Preserve the interrupt status
			Thread.currentThread().interrupt();
		}

	}

}
