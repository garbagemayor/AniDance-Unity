package anidance.anidance_android.table;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class Moves {

    public static HashMap<String, List<String>> moves;

	public List<String> getMoves(String step) {
		return moves.get(step);
	}

    public Moves() {

        moves = new HashMap<>();
        MovesHelperT.pushToMoves();
        MovesHelperR.pushToMoves();
        MovesHelperC.pushToMoves();
        MovesHelperW.pushToMoves();
    }

}
