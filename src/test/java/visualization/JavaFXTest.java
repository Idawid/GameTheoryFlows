package visualization;

import org.junit.Test;
import org.testfx.assertions.api.Assertions;
import org.testfx.framework.junit.ApplicationTest;

import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class JavaFXTest extends ApplicationTest {

    private Button button;

    // Will be called with {@code @Before} semantics, i. e. before each test method.
    @Override
    public void start(Stage stage) {
        button = new Button("click me!");
        button.setOnAction(actionEvent -> button.setText("clicked!"));
        stage.setScene(new Scene(new StackPane(button), 100, 100));
        stage.show();
    }

    @Test
    public void shouldContainButtonWithText() {
        Assertions.assertThat(button).hasText("click me!");
    }

    @Test
    public void whenButtonIsClickedTextChanges() {
        // when:
        clickOn(".button");

        // then:
        Assertions.assertThat(button).hasText("clicked!");
    }
}