package KosmoScan.services;

import KosmoScan.domain.Student;
import KosmoScan.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class StudentService {
    private final StudentRepository studentRepository;

    public Student findOrCreate(String firstname, String lastName, String middleName)  {
        return studentRepository.findByFirstNameAndLastNameAndMiddleName(firstname, lastName, middleName)
                .orElseGet(() -> {
                    Student student = new Student();
                    student.setFirstName(firstname);
                    student.setLastName(lastName);
                    student.setMiddleName(middleName);
                    return studentRepository.save(student);
                });
    }
}
