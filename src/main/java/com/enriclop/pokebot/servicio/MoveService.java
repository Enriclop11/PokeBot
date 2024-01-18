package com.enriclop.pokebot.servicio;

import com.enriclop.pokebot.modelo.Move;
import com.enriclop.pokebot.repositorio.IMoveRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MoveService {

    @Autowired
    private IMoveRepository moveRepository;

    public MoveService(IMoveRepository moveRepository) {
        this.moveRepository = moveRepository;
    }

    public List<Move> getMoves() {
        return moveRepository.findAll();
    }

    public Move getMoveById(Integer id) {
        return moveRepository.findById(id).get();
    }

    public Move saveMove(Move move) {
        return moveRepository.save(move);
    }

    public void deleteMoveById(Integer id) {
        moveRepository.deleteById(id);
    }

    public void saveMoves(List<Move> moves) {
        moveRepository.saveAll(moves);
    }
}

