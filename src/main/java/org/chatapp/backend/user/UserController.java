package org.chatapp.backend.user;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "${api.prefix}/users")
@Tag(name = "Users", description = "User presence and profile management")
public class UserController {

    private final UserService userService;




    @MessageMapping("/user/connect") // Receives message from clients sending to /app/user/connect
    @SendTo("/topic/active") // Send the response to all clients subscribe to /topic/active
    @Operation(summary = "WebSocket: user connects", description = "Marks a user as online and notifies subscribers at /topic/active")
    public UserDTO connect(@RequestBody UserDTO userDTO) {
        return userService.connect(userDTO);
    }



    @MessageMapping("/user/disconnect") // Receives message from clients sending to /app/user/disconnect
    @SendTo("/topic/active") // Send the response to all clients subscribe to /topic/active
    @Operation(summary = "WebSocket: user disconnects", description = "Marks a user as offline and notifies subscribers at /topic/active")
    public UserDTO disconnect(@RequestBody UserDTO userDTO) {
        return userService.logout(userDTO.getUsername());
    }


    @GetMapping("/online")
    @Operation(summary = "List online users")
    public ResponseEntity<List<UserDTO>> getOnlineUsers() {
        return ResponseEntity.ok(userService.getOnlineUsers());
    }



    @GetMapping("/search/{username}")
    @Operation(summary = "Search users by username")
    public ResponseEntity<List<UserDTO>> searchUsersByUsername(@PathVariable final String username) {
        return ResponseEntity.ok(userService.searchUsersByUsername(username));
    }



    @PostMapping("/avatar")
    @Operation(summary = "Upload user avatar")
    public ResponseEntity<UserDTO> uploadAvatar(@RequestParam final MultipartFile file,
                                                @RequestParam final String username) {
        return ResponseEntity.ok(userService.uploadAvatar(file, username));
    }

}
