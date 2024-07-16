package com.rocketseat.planner.participant;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

@Service
public class ParticipantService {
    public void register(List<String> participants, UUID tripId) { }
    public void triggerEmailConfirmation(UUID tripId) {}
}
