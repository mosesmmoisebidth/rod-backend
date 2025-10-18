package org.chatapp.backend.messageroommember;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "${api.prefix}/messageroommember")
@Tag(name = "Room Members", description = "Manage members of message rooms")
public class MessageRoomMemberController {

    private final MessageRoomMemberService messageRoomMemberService;



    @PostMapping("/update-last-seen/{roomId}/{username}")
    @Operation(summary = "Update member last-seen in a room")
    public ResponseEntity<MessageRoomMemberDTO> updateLastSeen(@PathVariable final UUID roomId,
                                                               @PathVariable final String username) {
        return ResponseEntity.ok(messageRoomMemberService.updateLastSeen(roomId, username));
    }



    @PostMapping("/add-members/{roomId}")
    @Operation(summary = "Add members to a room")
    public ResponseEntity<List<MessageRoomMemberDTO>> addMembers(@PathVariable final UUID roomId,
                                                           @RequestBody final List<MessageRoomMemberDTO> memberDTOS) {
        return ResponseEntity.ok(messageRoomMemberService.addMembers(roomId, memberDTOS));
    }



    @DeleteMapping("/remove-member/{roomId}/{memberId}")
    @Operation(summary = "Remove a member from a room")
    public ResponseEntity<Boolean> removeMember(@PathVariable final UUID roomId,
                                                             @PathVariable final String memberId) {
        return ResponseEntity.ok(messageRoomMemberService.removeMember(roomId, memberId));
    }



    @PostMapping("/make-admin/{roomId}/{memberId}")
    @Operation(summary = "Grant admin role to a member in a room")
    public ResponseEntity<MessageRoomMemberDTO> makeAdmin(@PathVariable final UUID roomId,
                                                          @PathVariable final String memberId) {
        return ResponseEntity.ok(messageRoomMemberService.adminAssign(roomId, memberId, true));
    }



    @PostMapping("/remove-admin/{roomId}/{memberId}")
    @Operation(summary = "Revoke admin role from a member in a room")
    public ResponseEntity<MessageRoomMemberDTO> removeAdmin(@PathVariable final UUID roomId,
                                                          @PathVariable final String memberId) {
        return ResponseEntity.ok(messageRoomMemberService.adminAssign(roomId, memberId, false));
    }

}
