import java.awt.*;
import java.awt.event.*;
import java.util.Random;
import java.util.ArrayList;
import javax.swing.*;
import javax.swing.event.MouseInputListener;

public class Minesweeper {
    //we do not use JButton directly cuz we have to add 2 attributes to the button(row,col)
    private class MineTile extends JButton{
        int r;
        int c;

        public MineTile(int r, int c){
            this.r=r;
            this.c=c;
        }
    }

    int TileSize=70;
    int numRows=8;
    int numCols=numRows;
    int boardWidth=TileSize*numCols;
    int boardHeight=TileSize*numRows;
    MineTile[][] board=new MineTile[numRows][numCols];
    ArrayList<MineTile> mineList;
    //count=>if clicked all tiles =>win
    int tileClicked=0;
    boolean gameOver=false;

    //create random mines
    int mineCount;
    Random random=new Random();

    JFrame frame=new JFrame("Minesweeper");
    JLabel textLabel=new JLabel();
    JPanel textPanel=new JPanel();
    JPanel boardPanel=new JPanel();

    public Minesweeper(){
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        mineCount=random.nextInt(2,8);
        frame.setSize(boardWidth,boardHeight);
        frame.setLocationRelativeTo(null);//show at the center of the screen
        frame.setResizable(false);
        frame.setLayout(new BorderLayout());
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        textLabel.setFont(new Font("Arial",Font.BOLD,25));
        textLabel.setHorizontalAlignment(JLabel.CENTER);
        textLabel.setText("Minesweeper"+Integer.toString(mineCount));
        textLabel.setOpaque(true);

        textPanel.setLayout(new BorderLayout());//设置组件之间的水平间距hgap和垂直间距vgap,需要使用north、south、east、west、center这五个常量之一来指定位置
        textPanel.setPreferredSize(new Dimension(boardWidth,40));
        textPanel.add(textLabel);//为什么显示不出来：frameSize太小了，加上TextPanel没有明确的高度，所以会被boardPanel完全覆盖掉，Swing默认不自动调整布局，center吃掉所有空间

        frame.add(textPanel,BorderLayout.NORTH);

        boardPanel.setLayout(new GridLayout(numRows,numCols));//8*8
        boardPanel.setBackground(Color.GRAY);
        frame.add(boardPanel);

        for (int r=0;r<numRows;r++){
            for (int c=0;c<numCols;c++){
                MineTile tile=new MineTile(r,c);
                board[r][c]=tile;

                tile.setFocusable(false);
                tile.setMargin(new Insets(0,0,0,0));
                tile.setFont(new Font("Arial Unicode MS",Font.PLAIN,45));
                //tile.setText("💣");//ctrl+cmd+space
                tile.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        if (gameOver){
                            return;
                        }
                        MineTile tile=(MineTile) e.getSource();
                        //通过 MouseEvent 的 getSource() 获取 “被点击的组件”（即当前点击的格子），并强制转换为自定义的 MineTile 类型（方便调用 MineTile 的专属方法 / 属性，如 getText()）。
                        //left click
                        if (e.getButton()==MouseEvent.BUTTON1){
                            if (tile.getText()==""){
                                if (mineList.contains(tile)){
                                    revealMine();//press mine
                                }else{
                                    checkMine(tile.r,tile.c);//no mine pressed but reminder the number of surrounding
                                }
                            }
                        }
                        //put a flag to the potential main
                        else if(e.getButton()==MouseEvent.BUTTON3){
                            if (tile.getText()==""&&tile.isEnabled()){
                                tile.setText("⛳");
                            }
                            else if(tile.getText()=="⛳"){
                                tile.setText("");//remove flag
                            }
                        }
                    }
                });

                boardPanel.add(tile);
            }
        }

        frame.setVisible(true);

        setMines();
    }
    public void setMines(){
        mineList=new ArrayList<MineTile>();

        //mineList.add(board[1][2]);tile with bomb

        int mineLeft=mineCount;
        while(mineLeft>0){
            int r=random.nextInt(numRows);
            int c=random.nextInt(numCols);
            MineTile tile=board[r][c];

            //check to prevent the same number exist
            if(!mineList.contains(tile)){
                mineList.add(tile);
                mineLeft-=1;
            }
        }
    }
    public void revealMine(){
        for (int i=0;i<mineList.size();++i){
            MineTile tile=mineList.get(i);
            tile.setText("💣");
        }
        gameOver=true;
        textLabel.setText("Game Over!");
    }
    public void checkMine(int r,int c){
        //make sure not exceed the boundary
        if (r<0||r>numRows||c<0||c>numCols){
            return;
        }
        MineTile tile=board[r][c];
        //if is clicked
        if (!tile.isEnabled()){
           return;
        }
        tile.setEnabled(false);//disable the button
        tileClicked+=1;

        int minesFound=0;

        //top3
        minesFound+=countMine(r-1,c-1);//top-left
        minesFound+=countMine(r-1,c);//top
        minesFound+=countMine(r-1,c+1);//top-right

        //left-right
        minesFound+=countMine(r,c-1);
        minesFound+=countMine(r,c+1);

        //bottom 3
        minesFound+=countMine(r+1,c-1);
        minesFound+=countMine(r+1,c);
        minesFound+=countMine(r+1,c+1);

        if (minesFound>0){
            tile.setText(Integer.toString(minesFound));
        }
        else{
            tile.setText("");

            //if a tile you picked with its surrounding neighbor tiles are all empty, disable all the empty tiles
            //top 3
            checkMine(r-1,c-1);
            checkMine(r-1,c);
            checkMine(r-1,c+1);

            //left-right
            checkMine(r,c-1);
            checkMine(r,c+1);

            //bottom 3
            checkMine(r+1,c-1);
            checkMine(r+1,c);
            checkMine(r+1,c+1);

        }
        if (tileClicked==numRows*numCols-mineList.size()){
            gameOver=true;
            textLabel.setText("You Win the Game!");
        }
    }
    public int countMine(int r,int c){
        if (r<0||r>numRows||c<0||c>numCols){
            return 0;
        }
        if (mineList.contains(board[r][c])){
            return 1;
        }
        return 0;
    }
}
