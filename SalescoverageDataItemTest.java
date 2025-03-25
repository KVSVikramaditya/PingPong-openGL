import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

@PostMapping("/submitDocument")
public ResponseEntity<?> submitDocument(@RequestBody String folderName) {
    String user = SecurityContextHolder.getContext().getAuthentication().getName();
    System.out.println("Logged-in user: " + user);
    // Proceed with group check if needed
    return ResponseEntity.ok("Success");
}
