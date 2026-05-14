import { Button, type ButtonProps } from './Button';

export type AppButtonProps = ButtonProps;

export function AppButton(props: AppButtonProps) {
  return <Button {...props} />;
}
