package edu.kennesaw.appdomain.service;

import edu.kennesaw.appdomain.dto.GenMessageDTO;
import edu.kennesaw.appdomain.entity.GeneralMessage;
import edu.kennesaw.appdomain.repository.GeneralMessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class GeneralMessageService {

    @Autowired
    private GeneralMessageRepository generalMessageRepository;

    public List<GenMessageDTO> getGeneralMessages() {
        List<GenMessageDTO> gmDTOs = new ArrayList<>();
        List<GeneralMessage> generalMessages =    generalMessageRepository.getAllByMessageIsNotNull();
        for (GeneralMessage gm : generalMessages) {
            gmDTOs.add(new GenMessageDTO(gm.username, gm.date, gm.message));
        }
        return gmDTOs;
    }

    public boolean postGeneralMessage(GenMessageDTO message) {
        GeneralMessage gm = new GeneralMessage();
        gm.setMessage(message.getMessage());
        gm.setDate(message.date);
        gm.setUsername(message.getUsername());
        generalMessageRepository.save(gm);
        return true;
    }
}
