package scenarios.mocks;

import engine.input.KeyState;

import java.util.List;

/** Generated Key State used for the Headless mode, see {@link MockCore}. */
public class MockKeys implements KeyState {

    private final List<Character> characters;

    /**
     * Constructs an instance using the given characters to treat as down keys.
     *
     * @param charactersToMock the given characters to treat as down keys.
     */
    public MockKeys(List<Character> charactersToMock) {
        this.characters = charactersToMock;
    }

    public MockKeys(char key) {
        this.characters = List.of(key);
    }

    @Override
    public List<Character> getDown() {
        return characters;
    }

    @Override
    public boolean isDown(char character) {
        return characters.contains(character);
    }
}
