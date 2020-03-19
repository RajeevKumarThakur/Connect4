
public class Controller implements Initializable {

	@FXML
	public GridPane rootGridPane;
	@FXML
	public Pane insertedDiscsPane;
	@FXML
	public Label playerNameLabel;
	@FXML
	public TextField  player1Field;
	@FXML
	public TextField player2Field;
	@FXML
	public Button nameButton;

	private static final int rows = 6;
	private static final int columns = 7;
	private static final int diameter = 80;
	private static final String discColor1 = "24303E";
	private static final String discColor2 = "4CAA88";
	private  static String player1 ="Player One";
	private  static String player2 = "Player Two";
	private boolean isPlayerOneTurn = true;

	private  boolean isAllowedToInserted = true;
	private Disc[][] insertedDiscsArray = new Disc[rows][columns];

	public void createPlayground () {

		nameButton.setOnAction(event -> {
			convert();
		});

		Shape rectangleWithHoles = createGameStructureGrid();
		rootGridPane.add(rectangleWithHoles,0,1);

		List<Rectangle> rectangleList = createClickableColumns();
		for(Rectangle rectangle : rectangleList) {
			rootGridPane.add(rectangle,0,1);
		}
	}

	private void convert() {
		player1 = player1Field.getText();
		player2 = player2Field.getText();
	}

	private Shape createGameStructureGrid () {

		Shape rectangleWithHoles = new Rectangle(diameter * (columns+1.2),diameter * (rows+1));

		for(int row=0;row<rows;row++) {
			for (int col = 0; col < columns; col++) {
				Circle circle = new Circle();
				circle.setRadius(diameter / 2);
				circle.setCenterX(diameter / 2);
				circle.setCenterY(diameter / 2);
				circle.setSmooth(true);

				circle.setTranslateX(col * (diameter+5) + diameter/2);
				circle.setCenterY(row * (diameter+5) + (diameter/2 + diameter/4  ) );

				rectangleWithHoles = Shape.subtract(rectangleWithHoles,circle);
			}
		}
		rectangleWithHoles.setFill(Color.WHITE);
		return rectangleWithHoles;
	}

	private List<Rectangle> createClickableColumns() {

		List<Rectangle> rectangleList = new ArrayList<>();

		for (int col = 0; col <columns ; col++) {
			Rectangle rectangle = new Rectangle(diameter,diameter * (rows+1));
			rectangle.setFill(Color.TRANSPARENT);
			rectangle.setTranslateX(col * (diameter+5) + diameter/2);
			rectangle.setOnMouseEntered(event -> rectangle.setFill(Color.valueOf("#eeeeee26")));
			rectangle.setOnMouseExited(event -> rectangle.setFill(Color.TRANSPARENT));

			final int column=col;
			rectangle.setOnMouseClicked(event -> {
				if(isAllowedToInserted) {
					isAllowedToInserted = false;
					insertDisc(new Disc(isPlayerOneTurn), column);
				}
			});

			rectangleList.add(rectangle);
		}
		return rectangleList;
	}

	private  void insertDisc(Disc disc,int column) {

		int row=rows-1;
		while (row >= 0) {

			if(getDiscIfPresent(row,column) == null)
				break;
			row--;
		}
		if(row < 0)
			return;

		insertedDiscsArray[row][column]=disc;
		insertedDiscsPane.getChildren().add(disc);
		disc.setTranslateX(column * (diameter+5) + diameter/2);

		int currentRow=row;
		TranslateTransition transition = new TranslateTransition(Duration.seconds(0.5),disc);
		transition.setToY(row * (diameter+5) + (diameter/4));
		transition.setOnFinished(event -> {

			isAllowedToInserted = true;
			if(gameEnded(currentRow,column)) {
				gameOver();
				return;
			}
			isPlayerOneTurn = !isPlayerOneTurn;
			playerNameLabel.setText(isPlayerOneTurn ? player1 : player2);
		});

		transition.play();
	}

	private boolean gameEnded(int row,int column) {

		List<Point2D> verticalPoint = IntStream.rangeClosed(row-3,row+3)
				.mapToObj(r -> new Point2D(r,column) )
				.collect(Collectors.toList());
		List<Point2D> horizontalPoints = IntStream.rangeClosed(column-3 , column+3)
				.mapToObj(c -> new Point2D(row,c) )
				.collect(Collectors.toList());

		Point2D startPoint1 = new Point2D(row - 3,column + 3);
		List<Point2D> diagonal1Points = IntStream.rangeClosed(0,6)
				.mapToObj(i -> startPoint1.add(i,-i))
				.collect(Collectors.toList());

		Point2D startPoint2 = new Point2D(row - 3,column - 3);
		List<Point2D> diagonal2Points = IntStream.rangeClosed(0,6)
				.mapToObj(i -> startPoint2.add(i , i))
				.collect(Collectors.toList());

		boolean isEnded = checkCombination(verticalPoint) || checkCombination(horizontalPoints)
				|| checkCombination(diagonal1Points) || checkCombination(diagonal2Points);

		return isEnded;
	}

	private boolean checkCombination(List<Point2D> points ) {

		int chain = 0;

		for(Point2D point : points) {

			int rowIndex = (int) point.getX();
			int columnIndex = (int) point.getY();
			Disc disc = getDiscIfPresent(rowIndex,columnIndex);

			if(disc != null && disc.isPlayerOneMove == isPlayerOneTurn) {
				chain++;
				if(chain == 4) {
					return true;
				}
			} else {
				chain =0;
			}
		}
		return  false;
	}

	private Disc getDiscIfPresent(int row,int column) {

		if(row < 0 || row >= rows || column < 0 || column >= columns)
			return null;

		return insertedDiscsArray [row] [column];
	}

	private void gameOver() {
		String winner = isPlayerOneTurn ? player1 : player2;
		System.out.println("Winner is " + winner);

		Alert alert = new Alert(Alert.AlertType.INFORMATION);
		alert.setTitle("Connect Four");
		alert.setHeaderText("Winner is " + winner);
		alert.setContentText("Want to play again ? ");

		ButtonType yesBtn = new ButtonType("Yes");
		ButtonType noBtn = new ButtonType("No, Exit");
		alert.getButtonTypes().setAll(yesBtn, noBtn);

		Platform.runLater(() -> {

			Optional<ButtonType> btnClicked = alert.showAndWait();

			if (btnClicked.isPresent() && btnClicked.get() == yesBtn) {
				resetGame();
			} else {
				Platform.exit();
				System.exit(0);
			}
		});
	}


	public void resetGame() {

		insertedDiscsPane.getChildren().clear();

		for (int row = 0; row <insertedDiscsArray.length ; row++) {
			for (int column = 0; column <insertedDiscsArray[row].length ; column++) {
				insertedDiscsArray [row][column] = null;
			}
		}
		isPlayerOneTurn = true;
		playerNameLabel.setText(player1);
		createPlayground();
	}

	private static class Disc extends Circle {

		private final boolean isPlayerOneMove;

		public Disc(boolean isPlayerOneMove) {
			this.isPlayerOneMove=isPlayerOneMove;
			setRadius(diameter/2);
			setFill(isPlayerOneMove ? Color.valueOf(discColor1) : Color.valueOf(discColor2));
			setCenterY(diameter/2);
			setCenterX(diameter/2);
		}
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {

	}
}
