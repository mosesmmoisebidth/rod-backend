package org.chatapp.backend.messageroom;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "${api.prefix}/messagerooms")
@Tag(name = "Message Rooms", description = "Create and query chat rooms")
public class MessageRoomController {

    private final MessageRoomService messageRoomService;



    @GetMapping("/find-chat-room")
    @Operation(summary = "Find a direct chat room by members", description = "Pass list of usernames; returns the room if it already exists")
    public ResponseEntity<MessageRoomDTO> findMessageRoomByMembers(@RequestParam final List<String> members) {
        return ResponseEntity.ok(messageRoomService.findMessageRoomByMembers(members));
    }



    @PostMapping("/create-chat-room")
    @Operation(summary = "Create a chat room", description = "Creates a 1-1 or group room with given members; username is the creator")
    public ResponseEntity<MessageRoomDTO> create(@RequestParam final List<String> members,
                                                 @RequestParam final String username) {
        return ResponseEntity.ok(messageRoomService.create(members, username));
    }



    @GetMapping("/find-chat-room-at-least-one-content/{username}")
    @Operation(summary = "List rooms with messages for a user", description = "Rooms are ordered by last message time and include unseen counts")
    public ResponseEntity<List<MessageRoomDTO>> findMessageRoomAtLeastOneContent(@PathVariable final String username) {
        return ResponseEntity.ok(messageRoomService.findMessageRoomAtLeastOneContent(username));
    }



    @GetMapping("/{roomId}")
    @Operation(summary = "Get a room by ID")
    public ResponseEntity<MessageRoomDTO> findById(@PathVariable final UUID roomId) {
        return ResponseEntity.ok(messageRoomService.findById(roomId));
    }



    @PostMapping("/update-group-name/{id}")
    @Operation(summary = "Update group room name")
    public ResponseEntity<MessageRoomDTO> updateGroupName(@PathVariable final UUID id,
                                                          @RequestBody final String name) {
        return ResponseEntity.ok(messageRoomService.updateGroupName(id, name));
    }

}
