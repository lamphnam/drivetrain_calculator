import { Input, type InputProps } from './Input';

export type LabeledInputProps = InputProps;

export function LabeledInput(props: LabeledInputProps) {
  return <Input {...props} />;
}
