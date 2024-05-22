import { render, screen, cleanup, getByTestId } from "@testing-library/react";
import { Login } from "./Login";

test('should rensder login component', () => {
    render(<Login/>);
    const loginElement = screen.getByTestId();
})