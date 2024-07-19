package com.rocketseat.planner.trip;

import java.sql.Date;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.apache.catalina.connector.Response;
import org.slf4j.helpers.Reporter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.ResponseEntity.BodyBuilder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.rocketseat.planner.activities.ActivityData;
import com.rocketseat.planner.activities.ActivityRequestPayload;
import com.rocketseat.planner.activities.ActivityResponse;
import com.rocketseat.planner.activities.ActivityService;
import com.rocketseat.planner.participant.Participant;
import com.rocketseat.planner.participant.ParticipantCreateResponse;
import com.rocketseat.planner.participant.ParticipantData;
import com.rocketseat.planner.participant.ParticipantRequestPayload;
import com.rocketseat.planner.participant.ParticipantService;

@RestController
@RequestMapping("/trips")
public class TripController {
    @Autowired
    private ParticipantService participantService;

    @Autowired
    private ActivityService activityService;

    @Autowired
    private TripRepository repository;
    
    @PostMapping
    public ResponseEntity<TripCreateResponse> createTrip(@RequestBody TripRequestPayload payload) {
        Trip trip = new Trip(payload);

        this.repository.save(trip);
        this.participantService.registerAllParticipants(payload.emails_to_invite(), trip);

        return ResponseEntity.ok(new TripCreateResponse(trip.getId()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Trip> getTripDetails(@PathVariable UUID id) {
        Optional<Trip> trip = this.repository.findById(id);

        if (!trip.isPresent()) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(trip.get());
    }

    @PutMapping("/{id}")
    public ResponseEntity<Trip> updateTrip(@PathVariable UUID id, @RequestBody TripRequestPayload payload) {
        Optional<Trip> trip = this.repository.findById(id);

        if (!trip.isPresent()) {
            return ResponseEntity.notFound().build();
        }

        Trip rawTrip = trip.get();

        rawTrip.setStartsAt(LocalDateTime.parse(payload.starts_at(), DateTimeFormatter.ISO_DATE_TIME));
        rawTrip.setEndsAt(LocalDateTime.parse(payload.ends_at(), DateTimeFormatter.ISO_DATE_TIME));
        rawTrip.setDestination(payload.destination());

        this.repository.save(rawTrip);

        return ResponseEntity.ok(rawTrip);
    }

    @GetMapping("/{id}/confirm")
    public ResponseEntity<Trip> confirmTrip(@PathVariable UUID id) {
        Optional<Trip> trip = this.repository.findById(id);
        
        if (!trip.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Trip rawTrip = trip.get();
        rawTrip.setIsConfirmed(true);

        this.repository.save(rawTrip);
        this.participantService.triggerEmailConfirmation(id);

        return ResponseEntity.ok(rawTrip);
    }

    @PostMapping("/{id}/invite")
    public ResponseEntity<ParticipantCreateResponse> inviteParticipant(@PathVariable UUID id, @RequestBody ParticipantRequestPayload payload) {
        Optional<Trip> trip = this.repository.findById(id);

        if (!trip.isPresent())
            return ResponseEntity.notFound().build();

        Trip rawTrip = trip.get();

        ParticipantCreateResponse participantResponse = this.participantService.registerParticipant(payload.email(), rawTrip);

        if (rawTrip.getIsConfirmed())
            this.participantService.triggerEmailConfirmationToParticipant(payload.email());

        return ResponseEntity.ok(participantResponse);
    }

    @GetMapping("/{id}/participants")
    public ResponseEntity<List<ParticipantData>> getAllParticipants(@PathVariable UUID id) {
        List<ParticipantData> participants = this.participantService.getAllParticipants(id);

        if (participants.isEmpty())
            return ResponseEntity.notFound().build();

        return ResponseEntity.ok(participants);
    }

    @PostMapping("/{id}/activities")
    public ResponseEntity<ActivityResponse> registerActivity(@PathVariable UUID id, @RequestBody ActivityRequestPayload payload) {
        Optional<Trip> trip = this.repository.findById(id);

        if (!trip.isPresent()) {
            return ResponseEntity.notFound().build();
        }

        Trip rawTrip = trip.get();

        ActivityResponse activityResponse = this.activityService.registerActivity(payload, rawTrip);

        return ResponseEntity.ok(activityResponse);
    }

    @GetMapping("/{id}/activities")
    public ResponseEntity<List<ActivityData>> getAllActivities(@PathVariable UUID id) {
        List<ActivityData> activities = this.activityService.getAllActivitiesFromId(id);

        if (activities.isEmpty())
            return ResponseEntity.notFound().build();

        return ResponseEntity.ok(activities);
    }
}
