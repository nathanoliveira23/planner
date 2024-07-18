package com.rocketseat.planner.participant;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.rocketseat.planner.trip.Trip;

@Service
public class ParticipantService {
    @Autowired
    private ParticipantRepository repository;

    public void registerAllParticipants(List<String> participantsToInvite, Trip trip) {
        List<Participant> participants = participantsToInvite.stream().map(email -> new Participant(trip, email)).toList();

        this.repository.saveAll(participants);
    }

    public ParticipantCreateResponse registerParticipant(String email, Trip trip) {
        Participant participant = new Participant(trip, email);

        this.repository.save(participant);

        return new ParticipantCreateResponse(participant.getId());
    }

    public void triggerEmailConfirmation(UUID tripId) {}

    public void triggerEmailConfirmationToParticipant(String email) {}
}
