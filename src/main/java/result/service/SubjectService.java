package result.service;

import result.dao.SubjectDAO;
import result.exception.DuplicateRecordException;
import result.exception.RecordNotFoundException;
import result.exception.ValidationException;
import result.model.Subject;

import java.util.List;
import java.util.Optional;

/**
 * Service class handling business logic and validations for academic subject management.
 */
public class SubjectService {

    private final SubjectDAO subjectDAO;

    public SubjectService(SubjectDAO subjectDAO) {
        this.subjectDAO = subjectDAO;
    }

    public boolean registerSubject(Subject subject) throws ValidationException, DuplicateRecordException {
        validateSubject(subject);

        // Check if subject with this code already exists
        if (subjectDAO.findByCode(subject.getSubjectCode().trim()).isPresent()) {
            throw new DuplicateRecordException("A subject with Code '" + subject.getSubjectCode().trim() + "' already exists.");
        }

        return subjectDAO.insert(subject);
    }

    public boolean updateSubject(Subject subject) throws ValidationException, RecordNotFoundException {
        validateSubject(subject);

        Optional<Subject> existingOpt = Optional.empty();
        if (subject.getSubjectId() > 0) {
            existingOpt = subjectDAO.findById(subject.getSubjectId());
        }
        if (!existingOpt.isPresent()) {
            existingOpt = subjectDAO.findByCode(subject.getSubjectCode().trim());
        }

        if (!existingOpt.isPresent()) {
            throw new RecordNotFoundException("Subject with Code '" + subject.getSubjectCode() + "' was not found.");
        }

        subject.setSubjectId(existingOpt.get().getSubjectId());
        return subjectDAO.update(subject);
    }

    public boolean deleteSubject(int subjectId) throws RecordNotFoundException {
        Optional<Subject> existing = subjectDAO.findById(subjectId);
        if (!existing.isPresent()) {
            throw new RecordNotFoundException("Subject with ID " + subjectId + " was not found.");
        }
        return subjectDAO.delete(subjectId);
    }

    public Subject getSubjectByCode(String subjectCode) throws RecordNotFoundException {
        if (subjectCode == null || subjectCode.trim().isEmpty()) {
            throw new RecordNotFoundException("Please provide a valid Subject Code.");
        }

        return subjectDAO.findByCode(subjectCode.trim())
                .orElseThrow(() -> new RecordNotFoundException("No subject found with Code: " + subjectCode.trim()));
    }

    public List<Subject> getAllSubjects() {
        return subjectDAO.findAll();
    }

    private void validateSubject(Subject subject) throws ValidationException {
        if (subject == null) {
            throw new ValidationException("Subject information cannot be empty.");
        }
        if (subject.getSubjectCode() == null || subject.getSubjectCode().trim().isEmpty()) {
            throw new ValidationException("Subject Code is required (e.g. CS101).");
        }
        if (subject.getSubjectName() == null || subject.getSubjectName().trim().isEmpty()) {
            throw new ValidationException("Subject Name is required (e.g. Java Programming).");
        }
        if (subject.getMaxMarks() <= 0) {
            throw new ValidationException("Maximum Marks must be greater than zero.");
        }
        if (subject.getPassMarks() < 0) {
            throw new ValidationException("Passing Marks cannot be negative.");
        }
        if (subject.getPassMarks() > subject.getMaxMarks()) {
            throw new ValidationException("Passing Marks (" + subject.getPassMarks() + ") cannot exceed Maximum Marks (" + subject.getMaxMarks() + ").");
        }
    }
}

