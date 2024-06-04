import React from 'react';
import { render, screen, fireEvent } from '@testing-library/react';
import '@testing-library/jest-dom';
import { Login } from './Login';

test('renders Login component', () => {
    render(<Login />);
    expect(screen.getByLabelText(/username/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/password/i)).toBeInTheDocument();
  });

  test('changes username input value', () => {
    render(<Login />);
    const usernameInput = screen.getByLabelText(/username/i);
    fireEvent.change(usernameInput, { target: { value: 'testuser' } });
    expect(usernameInput.value).toBe('testuser');
  });

  test('changes password input value', () => {
    render(<Login />);
    const passwordInput = screen.getByLabelText(/password/i);
    fireEvent.change(passwordInput, { target: { value: 'password' } });
    expect(passwordInput.value).toBe('password');
  });


//   test('submits the form', () => {
//     const handleSubmit = jest.fn((e) => e.preventDefault());
//     render(<Login />);
//     const form = screen.getByRole('form');
//     fireEvent.submit(form);
//     expect(handleSubmit).toHaveBeenCalled();
//   });