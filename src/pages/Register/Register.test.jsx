import React from 'react';
import { render, screen, fireEvent } from '@testing-library/react';
import '@testing-library/jest-dom';
import { Register } from './Register';


  test('renders Register component', () => {
    render(<Register />);
    expect(screen.getByLabelText(/username/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/password/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/full name/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/phone number/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/email/i)).toBeInTheDocument();
  });

  test('changes username input value', () => {
    render(<Register />);
    const usernameInput = screen.getByLabelText(/username/i);
    fireEvent.change(usernameInput, { target: { value: 'testuser' } });
    expect(usernameInput.value).toBe('testuser');
  });

  test('changes password input value', () => {
    render(<Register />);
    const passwordInput = screen.getByLabelText(/password/i);
    fireEvent.change(passwordInput, { target: { value: 'password' } });
    expect(passwordInput.value).toBe('password');
  });

  test('changes full name input value', () => {
    render(<Register />);
    const fullNameInput = screen.getByLabelText(/full name/i);
    fireEvent.change(fullNameInput, { target: { value: 'John Doe' } });
    expect(fullNameInput.value).toBe('John Doe');
  });

  test('changes phone number input value', () => {
    render(<Register />);
    const phoneNumberInput = screen.getByLabelText(/phone number/i);
    fireEvent.change(phoneNumberInput, { target: { value: '1234567890' } });
    expect(phoneNumberInput.value).toBe('1234567890');
  });

  test('changes email input value', () => {
    render(<Register />);
    const emailInput = screen.getByLabelText(/email/i);
    fireEvent.change(emailInput, { target: { value: 'john.doe@example.com' } });
    expect(emailInput.value).toBe('john.doe@example.com');
  });

//   test('submits the form', () => {
//     const handleSubmit = jest.fn((e) => e.preventDefault());
//     render(<Register />);
//     const form = screen.getByRole('form');
//     fireEvent.submit(form);
//     expect(handleSubmit).toHaveBeenCalled();
//   });