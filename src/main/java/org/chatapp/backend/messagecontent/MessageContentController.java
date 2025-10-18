package org.chatapp.backend.messagecontent;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.chatapp.backend.messageroom.MessageRoomService;
import org.chatapp.backend.messageroommember.MessageRoomMemberDTO;
import org.chatapp.backend.messageroommember.MessageRoomMemberService;
import org.chatapp.backend.user.UserDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "${api.prefix}/messagecontents")
@Tag(name = "Messages", description = "Send and query message contents")
public class MessageContentController {

    private final MessageContentService messageContentService;
    private final MessageRoomMemberService messageRoomMemberService;
    private final SimpMessagingTemplate simpMessagingTemplate;



    @GetMapping("/{roomId}")
    @Operation(summary = "List messages by room")
    public ResponseEntity<List<MessageContentDTO>> getMessagesByRoomId(@PathVariable final UUID roomId) {
        return ResponseEntity.ok(messageContentService.getMessagesByRoomId(roomId));
    }



    @MessageMapping("/send-message")
    @Operation(summary = "WebSocket: send a message", description = "Saves the message then sends it to each room member’s /user/queue/messages")
    public void sendMessage(@RequestBody MessageContentDTO messageContentDTO) {
        final MessageContentDTO saved = messageContentService.save(messageContentDTO);
        final List<MessageRoomMemberDTO> members = messageRoomMemberService.findByMessageRoomId(messageContentDTO.getMessageRoomId());
        members.forEach(member -> {
            simpMessagingTemplate.convertAndSendToUser(
                    member.getUsername(),
                    "/queue/messages",
                    saved
            );
        });
    }

}
