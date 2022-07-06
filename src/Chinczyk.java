import java.util.Scanner;

public class Chinczyk {
    static int players; //ilość graczy
    static int[][] pawns = new int[16][2]; // pionki {a-d, 0-39}
    static int[][] bases = new int[4][4]; // pionki w bazie, np. {a, a, a, a}
    static int[][] won = new int[4][4]; // pionki w domku, j.w.
    static int turn = 'a' + (int)(Math.random()*players);

    public static void main(String[] args) {
	    start();
        //start(args);
    }

    public static void start() {
        players = getPlayerCount();
        initBases(players);
        initWon(players);

        boolean stillPlaying = true;

        while(stillPlaying) {

            System.out.println("Tura gracza " + (char)turn);
            int rolled = rollTheDice();
            if(!playerPawnExists() && rolled != 6) {
                passTheTurn();
                continue;
            }

            renderBoard(pawns);

            int distance = 0;
            boolean invalid = false;
            boolean willMove = true;
            boolean multipleRoll = false;

            while(rolled == 6 || invalid || multipleRoll) {
                invalid = false;
                switch(getSixChoice()) {
                    case 1:
                        multipleRoll = true;
                        willMove = false;
                        distance += rolled;

                        rolled = rollTheDice();
                        if(rolled != 6) {
                            willMove = true;
                            multipleRoll = false;
                        }
                        break;
                    case 2:
                        if(canMoveOut()) {
                            moveOut();
                            rolled = 0;
                            multipleRoll = false;
                            willMove = false;
                        }
                        else {
                            System.out.println("Nie masz pionkow w bazie!");
                            invalid = true;
                        }
                        break;
                    case 3:
                        willMove = true;
                        multipleRoll = false;
                        distance += rolled;
                        rolled = 0;
                        break;
                    default:
                        invalid = true;
                        break;
                }
            }
            if(willMove) {
                distance += rolled;
                rolled = 0;

                int index;
                do {
                    index = getMoveIndex();
                } while(!playerPawnExists(index));

                for(int i=0; i<pawns.length; i++) {
                    if(pawns[i][0] == turn && pawns[i][1] == index) {
                        if(canHome(index, distance)) {
                            addToHome();
                            pawns[i] = new int[]{0, 0};
                            break;
                        }
                        int destination = (index + distance) % 40;
                        captureAll(destination);
                        pawns[i][1] = destination;
                        break;
                    }
                }
            }
            if(isAWinner()) {
                stillPlaying = false;
                System.out.println("Gracz " + (char)turn + " wygrywa!");
            }
            passTheTurn();
        }
    }


    // do testów
    public static void start(int players, int[][] pawns, int[] scores, int[] toMove) {

        initBases(players);
        initWon(players);

        boolean stillPlaying = true;
        int n=0;
        while(stillPlaying && n<scores.length && n<toMove.length) {

            System.out.println("Tura gracza " + (char)turn);
            int rolled = scores[n];
            if(!playerPawnExists() && rolled != 6) {
                passTheTurn();
                continue;
            }

            renderBoard(pawns);

            int distance = rolled;
            boolean invalid = false;
            boolean willMove = true;
            boolean multipleRoll = false;

            int index = toMove[n];

            for(int i=0; i<pawns.length; i++) {
                if(pawns[i][0] == turn && pawns[i][1] == index) {
                    if(canHome(index, distance)) {
                        addToHome();
                        pawns[i] = new int[]{0, 0};
                        break;
                    }
                    int destination = (index + distance) % 40;
                    captureAll(destination);
                    pawns[i][1] = destination;
                    break;
                }
            }
            if(isAWinner()) {
                stillPlaying = false;
                System.out.println("Gracz " + (char)turn + " wygrywa!");
            }
            passTheTurn();
            n++;
        }

    }


    public static boolean canHome(int pos, int dist) {
        int home = (turn - 'a') * 10;

        while(dist > 0) {
            pos = (pos + 1) % 40;
            if(pos == home) return true;
            dist--;
        }
        return false;
    }

    public static int getPlayerCount() {
        int players;
        do {
            Scanner sc = new Scanner(System.in);
            System.out.println("Podaj ilosc graczy: ");
            players = sc.nextInt();
        } while (players < 2 || players > 4);
        return players;
    }

    public static int getSixChoice() {
        boolean valid = true;
        int value;
        Scanner sc = new Scanner(System.in);
        do {
            System.out.println("Co chcesz wykonać?\n" +
                    "1: ponowny rzut\n" +
                    "2: wprowadź swój pionek na mapę\n" +
                    "3: porusz swoim pionkiem\n");
            value = sc.nextInt();
        } while(value < 1 || value > 3);
        return value;
    }

    public static int rollTheDice() {
        int result = (int)(Math.random()*2+5); //*6+1
        System.out.println("Wylosowano: " + result);
        return result;
    }

    public static void passTheTurn() {
        int var = turn + 1;
        if(var > 'a' + players - 1)
            turn = 'a';
        else turn = var;
    }

    public static boolean canMoveOut() {
        for(int i=0; i<bases[turn - 'a'].length; i++) {
            if(bases[turn - 'a'][i] != 0) return true;
        }
        return false;
    }

    public static void moveOut() {
        for(int i=0; i<bases[turn - 'a'].length; i++) {
            if(bases[turn - 'a'][i] != ' ') {
                bases[turn - 'a'][i] = ' ';
                for(int j=0; j<pawns.length; j++) {
                    if(pawns[j][0] == 0) {
                        pawns[j] = new int[]{turn, (turn - 'a') * 10};
                        break;
                    }
                }
                break;
            }
        }
    }

    public static int getMoveIndex() {
        int index;
        do {
            Scanner sc = new Scanner(System.in);
            System.out.println("Wybierz pionek do przesuniecia: ");
            index = sc.nextInt();
        } while (index < 0 || index > 39);
        return index;
    }

    public static boolean playerPawnExists(int index) {
        for(int i=0; i<pawns.length; i++) {
            if(pawns[i][0] == turn) {
                    if(pawns[i][1] == index)
                        return true;
            }
        }
        return false;
    }

    public static boolean playerPawnExists() {
        for(int i=0; i<pawns.length; i++)
            if(pawns[i][0] == turn)
                return true;
        return false;
    }

    public static void captureAll(int destination) {

        for(int i=0; i<pawns.length; i++) {
            if(pawns[i][1] == destination && pawns[i][0] != 0) {
                int color = pawns[i][0];
                addToBase(color);
                System.out.println("Bicie!");
                pawns[i] = new int[]{0, 0};
            }
        }
    }

    public static void addToBase(int color) {
        for(int i=0; i<4; i++) {
            if(bases[color - 'a'][i] == ' ') {
                bases[color - 'a'][i] = color;
                return;
            }
        }
    }

    public static void addToHome() {
        for(int i=0; i<4; i++) {
            if(won[turn - 'a'][i] == ' ') {
                won[turn - 'a'][i] = turn;
                return;
            }
        }
    }

    public static boolean isAWinner() {
        for(int i=0; i<won[turn - 'a'].length; i++) {
            if(won[turn - 'a'][i] == ' ')
                return false;
        }
        return true;
    }

    public static void renderBoard(int[][] pawns) {

        char[][] board = new char[13][27]; //w tej tablicy 28x13 sa wszystkie pola do wyprintowania

        int[][] fields = new int[][] { //
                //y, x
                {15, 1}, {15, 2}, {15, 3}, {15, 4},
                {15, 5},
                {17, 5}, {19, 5}, {21, 5},
                {23, 5},
                {23, 6},
                {23, 7},
                {21, 7}, {19, 7}, {17, 7},
                {15, 7},
                {15, 8}, {15, 9}, {15, 10}, {15, 11},
                {13, 11},
                {11, 11},
                {11, 10}, {11, 9}, {11, 8},
                {11, 7},
                {9, 7}, {7, 7}, {5, 7}, {3, 7},
                {3, 6},
                {3, 5},
                {5, 5}, {7, 5}, {9, 5},
                {11, 5},
                {11, 4}, {11, 3}, {11, 2}, {11, 1},
                {13, 1}
        };

        int[][] helpers = new int[][] {
                {15, 0, '0'},
                {25, 7, '1'}, {26, 7, '0'},
                {10, 12, '2'}, {11, 12, '0'},
                {0, 5, '3'}, {1, 5, '0'}
        };

        int[][] homes = new int[][] {
                {3, 1, bases[3][0]}, {5, 1, bases[3][1]}, {3, 2, bases[3][2]}, {5, 2, bases[3][3]},
                {21, 1, bases[0][0]}, {23, 1, bases[0][1]}, {21, 2, bases[0][2]}, {23, 2, bases[0][3]},
                {3, 10, bases[2][0]}, {5, 10, bases[2][1]}, {3, 11, bases[2][2]}, {5, 11, bases[2][3]},
                {21, 10, bases[1][0]}, {23, 10, bases[1][1]}, {21, 11, bases[1][2]}, {23, 11, bases[1][3]}
        };

        int[][] winners = new int[][] {
                {13, 2, won[0][0]}, {13, 3, won[0][1]}, {13, 4, won[0][2]}, {13, 5, won[0][3]},
                {13, 7, won[2][3]}, {13, 8, won[2][2]}, {13, 9, won[2][1]}, {13, 10, won[2][0]},
                {5, 6, won[3][0]}, {7, 6, won[3][1]}, {9, 6, won[3][2]}, {11, 6, won[3][3]},
                {15, 6, won[1][3]}, {17, 6, won[1][2]}, {19, 6, won[1][1]}, {21, 6, won[1][0]},
        };

        for(int y=0; y<13; y++) for (int x=0; x<27; x++) board[y][x] = ' ';

        for(int i=0; i<helpers.length; i++) board[ helpers[i][1] ][ helpers[i][0] ] = (char) helpers[i][2];
        for(int i=0; i<homes.length; i++) board[ homes[i][1] ][ homes[i][0] ] = (char) homes[i][2];
        for(int i=0; i<winners.length; i++) board[ winners[i][1] ][ winners[i][0] ] = (char) winners[i][2];

        for(int i=0; i<40; i++)
        {
            char toPrint = 'x';
            for(int j=0; j<16; j++) {
                if(pawns[j][0] != 0 && pawns[j][1] == i) {
                    toPrint = (char) pawns[j][0];
                    break;
                }
            }
            board[ fields[i][1] ][ fields[i][0] ] = toPrint;
        }

        for(int y = 0; y < 13; y++) System.out.println(new String(board[y]));
    }

    public static void initBases(int players) {
        for(int i=0; i<4; i++) {
            for(int j=0; j<4; j++) {
                if(i > players-1)
                    bases[i][j] = ' ';
                else
                    bases[i][j] = 'a' + i;
            }
        }
    }
    public static void initWon(int players) {
        for(int i=0; i<won.length; i++) {
            won[i][0] = 'a' + i;
            for(int j=0; j<won[i].length; j++) {
                won[i][j] = ' ';
            }
        }
    }

}


