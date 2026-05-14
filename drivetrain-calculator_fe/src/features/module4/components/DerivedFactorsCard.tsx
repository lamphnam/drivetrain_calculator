import { Card } from '@/components/ui/Card';
import { KeyValueList } from '@/components/ui/KeyValueList';
import { UI_TEXT } from '@/constants/uiText';
import type { Module4CalculationResponseDto } from '@/types/api/module4';

type Props = {
  factors: Module4CalculationResponseDto['derivedFactors'];
};

export function DerivedFactorsCard({ factors }: Props) {
  return (
    <Card
      title={UI_TEXT.module4.factorsTitle}
      description={UI_TEXT.module4.factorsDescription}
      tone="muted"
    >
      <KeyValueList
        items={[
          { label: UI_TEXT.module4Fields.epsilonAlpha, value: `${factors.epsilonAlpha.toFixed(3)}` },
          { label: UI_TEXT.module4Fields.zEpsilon, value: `${factors.zEpsilon.toFixed(3)}` },
          { label: UI_TEXT.module4Fields.yEpsilon, value: `${factors.yEpsilon.toFixed(3)}` },
          { label: UI_TEXT.module4Fields.yF1, value: `${factors.yF1.toFixed(3)}` },
          { label: UI_TEXT.module4Fields.yF2, value: `${factors.yF2.toFixed(3)}` },
          { label: UI_TEXT.module4Fields.loadFactorKh, value: `${factors.loadFactorKh.toFixed(3)}` },
          { label: UI_TEXT.module4Fields.loadFactorKf, value: `${factors.loadFactorKf.toFixed(3)}` },
        ]}
      />
    </Card>
  );
}
